package app.accommodationbookingservice.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.accommodationbookingservice.model.Accommodation;
import app.accommodationbookingservice.service.AccommodationService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

class AccommodationControllerTest {

    @Mock
    private AccommodationService svc;

    @InjectMocks
    private AccommodationController ctrl;

    private Accommodation acc;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        acc = Accommodation.builder()
                .id(42L)
                .location("XYZ")
                .dailyRate(BigDecimal.valueOf(150))
                .availability(3)
                .build();
    }

    @Test
    void list_returnsAll() {
        when(svc.findAll()).thenReturn(List.of(acc));
        ResponseEntity<List<Accommodation>> resp = ctrl.list();
        assertEquals(1, resp.getBody().size());
        verify(svc).findAll();
    }

    @Test
    void create_withManagerRole_succeeds() {
        when(svc.create(acc)).thenReturn(acc);
        ResponseEntity<Accommodation> resp = ctrl.create(acc);
        assertEquals(acc, resp.getBody());
        verify(svc).create(acc);
    }

    @Test
    void delete_invokesService() {
        ctrl.delete(42L);
        verify(svc).delete(42L);
    }
}
