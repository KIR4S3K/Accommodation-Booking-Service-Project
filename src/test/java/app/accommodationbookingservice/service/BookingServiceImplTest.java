package app.accommodationbookingservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.accommodationbookingservice.exception.AlreadyCanceledException;
import app.accommodationbookingservice.model.Accommodation;
import app.accommodationbookingservice.model.Booking;
import app.accommodationbookingservice.model.Payment;
import app.accommodationbookingservice.model.enums.BookingStatus;
import app.accommodationbookingservice.repository.AccommodationRepository;
import app.accommodationbookingservice.repository.BookingRepository;
import app.accommodationbookingservice.repository.PaymentRepository;
import app.accommodationbookingservice.service.impl.BookingServiceImpl;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

class BookingServiceImplTest {

    @Mock private BookingRepository bookingRepo;
    @Mock private AccommodationRepository accommodationRepo;
    @Mock private PaymentRepository paymentRepo;
    @Mock private NotificationService notificationService;

    @InjectMocks private BookingServiceImpl service;

    private Booking booking;
    private Accommodation acc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        acc = Accommodation.builder()
                .id(1L)
                .availability(1)
                .build();

        booking = Booking.builder()
                .id(100L)
                .accommodationId(1L)
                .userId(50L)
                .checkInDate(LocalDate.now().plusDays(1))
                .checkOutDate(LocalDate.now().plusDays(2))
                .status(BookingStatus.PENDING)
                .build();
    }

    // create()

    @Test
    void create_withPendingPayments_throwsBadRequest() {
        when(paymentRepo.findPendingByUserId(50L)).thenReturn(List.of(new Payment()));

        var ex = assertThrows(ResponseStatusException.class,
                () -> service.create(booking));

        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("pending payments"));
    }

    @Test
    void create_withInvalidDates_throwsBadRequest() {
        booking.setCheckInDate(LocalDate.now().plusDays(3));
        booking.setCheckOutDate(LocalDate.now().plusDays(2));
        when(paymentRepo.findPendingByUserId(50L)).thenReturn(List.of());

        var ex = assertThrows(ResponseStatusException.class,
                () -> service.create(booking));

        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Check-in date must be before"));
    }

    @Test
    void create_withOverlap_throwsBadRequest() {
        when(paymentRepo.findPendingByUserId(50L)).thenReturn(List.of());
        Booking other = Booking.builder()
                .checkInDate(booking.getCheckInDate())
                .checkOutDate(booking.getCheckOutDate())
                .status(BookingStatus.CONFIRMED)
                .build();
        when(bookingRepo.findByAccommodationIdAndStatusIn(eq(1L), anyList()))
                .thenReturn(List.of(other));

        var ex = assertThrows(ResponseStatusException.class,
                () -> service.create(booking));

        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("already booked"));
    }

    @Test
    void create_noAccommodation_throwsNotFound() {
        when(paymentRepo.findPendingByUserId(50L)).thenReturn(List.of());
        when(bookingRepo.findByAccommodationIdAndStatusIn(eq(1L), anyList()))
                .thenReturn(List.of());
        when(accommodationRepo.findByIdForUpdate(1L)).thenReturn(Optional.empty());

        var ex = assertThrows(ResponseStatusException.class,
                () -> service.create(booking));

        assertEquals(404, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Accommodation not found"));
    }

    @Test
    void create_noAvailability_throwsBadRequest() {
        acc.setAvailability(0);
        when(paymentRepo.findPendingByUserId(50L)).thenReturn(List.of());
        when(bookingRepo.findByAccommodationIdAndStatusIn(eq(1L), anyList()))
                .thenReturn(List.of());
        when(accommodationRepo.findByIdForUpdate(1L))
                .thenReturn(Optional.of(acc));

        var ex = assertThrows(ResponseStatusException.class,
                () -> service.create(booking));

        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("No availability"));
    }

    @Test
    void create_success_decrementsAndNotifies() {
        when(paymentRepo.findPendingByUserId(50L)).thenReturn(List.of());
        when(bookingRepo.findByAccommodationIdAndStatusIn(eq(1L), anyList()))
                .thenReturn(List.of());
        when(accommodationRepo.findByIdForUpdate(1L)).thenReturn(Optional.of(acc));
        when(bookingRepo.save(any())).thenAnswer(i -> {
            Booking b = i.getArgument(0);
            b.setId(123L);
            return b;
        });

        var saved = service.create(booking);

        assertEquals(BookingStatus.PENDING, saved.getStatus());
        assertEquals(0, acc.getAvailability());
        verify(accommodationRepo).save(acc);
        verify(notificationService).notify("New booking created: " + 123L);
    }

    // update()

    @Test
    void update_nonExisting_throwsNotFound() {
        when(bookingRepo.findById(100L)).thenReturn(Optional.empty());

        var ex = assertThrows(ResponseStatusException.class,
                () -> service.update(100L, booking));

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void update_withOverlap_throwsBadRequest() {
        when(bookingRepo.findById(100L)).thenReturn(Optional.of(booking));
        booking.setStatus(BookingStatus.PENDING);
        when(bookingRepo.findByAccommodationIdAndStatusIn(eq(1L), anyList()))
                .thenReturn(List.of(booking));

        var ex = assertThrows(ResponseStatusException.class,
                () -> service.update(100L, booking));

        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("already booked"));
    }

    @Test
    void update_success_notifies() {
        when(bookingRepo.findById(100L)).thenReturn(Optional.of(booking));
        when(bookingRepo.findByAccommodationIdAndStatusIn(eq(1L), anyList()))
                .thenReturn(List.of());
        when(bookingRepo.save(any())).thenReturn(booking);

        var updated = service.update(100L, booking);

        assertEquals(booking, updated);
        verify(notificationService).notify("Booking updated: " + booking.getId());
    }

    // cancel()

    @Test
    void cancel_nonExisting_throwsNotFound() {
        when(bookingRepo.findById(100L)).thenReturn(Optional.empty());

        var ex = assertThrows(ResponseStatusException.class,
                () -> service.cancel(100L));

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void cancel_alreadyCanceled_throws() {
        booking.setStatus(BookingStatus.CANCELED);
        when(bookingRepo.findById(100L)).thenReturn(Optional.of(booking));

        assertThrows(AlreadyCanceledException.class,
                () -> service.cancel(100L));
    }

    @Test
    void cancel_noAccommodation_throwsNotFound() {
        booking.setStatus(BookingStatus.CONFIRMED);
        when(bookingRepo.findById(100L)).thenReturn(Optional.of(booking));
        when(accommodationRepo.findByIdForUpdate(1L)).thenReturn(Optional.empty());

        var ex = assertThrows(ResponseStatusException.class,
                () -> service.cancel(100L));

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void cancel_success_restoresAndNotifies() {
        booking.setStatus(BookingStatus.CONFIRMED);
        when(bookingRepo.findById(100L)).thenReturn(Optional.of(booking));
        when(accommodationRepo.findByIdForUpdate(1L)).thenReturn(Optional.of(acc));

        service.cancel(100L);

        assertEquals(BookingStatus.CANCELED, booking.getStatus());
        assertEquals(2, acc.getAvailability());
        verify(accommodationRepo).save(acc);
        verify(notificationService).notify("Booking canceled: " + booking.getId());
    }

    // findById, findByUser, findAll

    @Test
    void findById_nonExisting_throwsNotFound() {
        when(bookingRepo.findById(100L)).thenReturn(Optional.empty());

        var ex = assertThrows(ResponseStatusException.class,
                () -> service.findById(100L));

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void findById_existing_returns() {
        when(bookingRepo.findById(100L)).thenReturn(Optional.of(booking));
        assertEquals(booking, service.findById(100L));
    }

    @Test
    void findByUser_returnsList() {
        when(bookingRepo.findByUserId(50L)).thenReturn(List.of(booking));
        var list = service.findByUser(50L);
        assertEquals(1, list.size());
        assertEquals(booking, list.get(0));
    }

    @Test
    void findAll_returnsList() {
        when(bookingRepo.findAll()).thenReturn(List.of(booking));
        var all = service.findAll();
        assertEquals(1, all.size());
        assertEquals(booking, all.get(0));
    }
}
