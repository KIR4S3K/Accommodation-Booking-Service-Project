package app.accommodationbookingservice.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.accommodationbookingservice.dto.AccommodationDto;
import app.accommodationbookingservice.dto.CreateAccommodationDto;
import app.accommodationbookingservice.mapper.AccommodationMapper;
import app.accommodationbookingservice.model.Accommodation;
import app.accommodationbookingservice.service.AccommodationService;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class AccommodationControllerTest {

    @Mock
    private AccommodationService svc;

    @Mock
    private AccommodationMapper mapper;

    @InjectMocks
    private AccommodationController ctrl;

    private Accommodation acc;
    private AccommodationDto accDto;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);

        acc = Accommodation.builder()
                .id(42L)
                .location("XYZ")
                .dailyRate(BigDecimal.valueOf(150))
                .availability(3)
                .build();

        accDto = new AccommodationDto();
        accDto.setId(42L);
        accDto.setLocation("XYZ");
        accDto.setDailyRate(BigDecimal.valueOf(150));
        accDto.setAvailability(3);
    }

    @Test
    void list_returnsAll() {
        when(svc.findAll()).thenReturn(List.of(acc));
        when(mapper.toDto(acc)).thenReturn(accDto);

        ResponseEntity<List<AccommodationDto>> resp = ctrl.list();
        assertEquals(1, resp.getBody().size());
        assertEquals(accDto, resp.getBody().get(0));

        verify(svc).findAll();
        verify(mapper).toDto(acc);
    }

    @Test
    void list_returnsEmpty() {
        when(svc.findAll()).thenReturn(Collections.emptyList());

        ResponseEntity<List<AccommodationDto>> resp = ctrl.list();
        assertTrue(resp.getBody().isEmpty());

        verify(svc).findAll();
    }

    @Test
    void create_withManagerRole_succeeds() {
        CreateAccommodationDto createDto = new CreateAccommodationDto();
        createDto.setLocation("XYZ");
        createDto.setDailyRate(BigDecimal.valueOf(150));
        createDto.setAvailability(3);

        when(mapper.toEntity(createDto)).thenReturn(acc);
        when(svc.create(acc)).thenReturn(acc);
        when(mapper.toDto(acc)).thenReturn(accDto);

        ResponseEntity<AccommodationDto> resp = ctrl.create(createDto);
        assertEquals(accDto, resp.getBody());

        verify(mapper).toEntity(createDto);
        verify(svc).create(acc);
        verify(mapper).toDto(acc);
    }

    @Test
    void delete_invokesService() {
        ResponseEntity<Void> resp = ctrl.delete(42L);
        assertEquals(HttpStatus.NO_CONTENT, resp.getStatusCode());
        verify(svc).delete(42L);
    }

    @Test
    void delete_notFound_returns404() {
        doThrow(new RuntimeException("Not found")).when(svc).delete(999L);

        ResponseEntity<Void> resp = ctrl.delete(999L);
        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());

        verify(svc).delete(999L);
    }
}
