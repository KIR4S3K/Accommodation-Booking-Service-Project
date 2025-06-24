package app.accommodationbookingservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepo;

    @Mock
    private AccommodationRepository accommodationRepo;

    @Mock
    private PaymentRepository paymentRepo;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private BookingServiceImpl service;

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
                .build();
    }

    @Test
    void create_whenPendingPaymentsExist_throws() {
        Payment dummyPayment = new Payment();
        when(paymentRepo.findPendingByUserId(50L)).thenReturn(List.of(dummyPayment));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.create(booking));

        assertTrue(ex.getMessage().contains("pending payments"));
    }

    @Test
    void create_whenOverlap_throws() {
        when(paymentRepo.findPendingByUserId(50L)).thenReturn(List.of());
        Booking other = Booking.builder()
                .checkInDate(booking.getCheckInDate())
                .checkOutDate(booking.getCheckOutDate())
                .status(BookingStatus.CONFIRMED)
                .build();

        when(bookingRepo.findByAccommodationIdAndStatusIn(eq(1L), anyList()))
                .thenReturn(List.of(other));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.create(booking));

        assertTrue(ex.getMessage().contains("already booked"));
    }

    @Test
    void create_decrementsAvailability_andNotifies() {
        when(paymentRepo.findPendingByUserId(50L)).thenReturn(List.of());
        when(bookingRepo.findByAccommodationIdAndStatusIn(eq(1L), anyList()))
                .thenReturn(List.of());
        when(accommodationRepo.findById(1L)).thenReturn(Optional.of(acc));
        when(bookingRepo.save(any())).thenAnswer(i -> {
            Booking b = i.getArgument(0);
            b.setId(123L);
            return b;
        });

        Booking saved = service.create(booking);

        assertEquals(BookingStatus.PENDING, saved.getStatus());
        assertEquals(0, acc.getAvailability());
        verify(accommodationRepo).save(acc);
        verify(notificationService).notify("New booking created: " + saved.getId());
    }

    @Test
    void cancel_restoresAvailability_andNotifies() {
        booking.setStatus(BookingStatus.CONFIRMED);
        when(bookingRepo.findById(100L)).thenReturn(Optional.of(booking));
        when(accommodationRepo.findById(1L)).thenReturn(Optional.of(acc));

        service.cancel(100L);

        assertEquals(BookingStatus.CANCELED, booking.getStatus());
        assertEquals(2, acc.getAvailability());
        verify(notificationService).notify("Booking canceled: " + booking.getId());
    }
}
