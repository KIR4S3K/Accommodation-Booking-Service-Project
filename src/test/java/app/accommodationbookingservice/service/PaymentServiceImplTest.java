package app.accommodationbookingservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.accommodationbookingservice.exception.EntityNotFoundException;
import app.accommodationbookingservice.exception.PaymentException;
import app.accommodationbookingservice.model.Accommodation;
import app.accommodationbookingservice.model.Booking;
import app.accommodationbookingservice.model.Payment;
import app.accommodationbookingservice.model.enums.BookingStatus;
import app.accommodationbookingservice.model.enums.PaymentStatus;
import app.accommodationbookingservice.repository.AccommodationRepository;
import app.accommodationbookingservice.repository.BookingRepository;
import app.accommodationbookingservice.repository.PaymentRepository;
import app.accommodationbookingservice.service.impl.PaymentServiceImpl;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {
    @Mock
    private PaymentRepository paymentRepo;

    @Mock
    private BookingRepository bookingRepo;

    @Mock
    private AccommodationRepository accommodationRepo;

    @InjectMocks
    private PaymentServiceImpl service;

    private Payment payment;
    private Booking booking;
    private Accommodation accommodation;

    @BeforeEach
    void init() {
        payment = Payment.builder()
                .id(5L)
                .bookingId(200L)
                .status(PaymentStatus.PENDING)
                .sessionId("sess_123")
                .amountToPay(BigDecimal.TEN)
                .build();

        booking = Booking.builder()
                .id(200L)
                .accommodationId(300L)
                .checkInDate(LocalDate.of(2025, 1, 1))
                .checkOutDate(LocalDate.of(2025, 1, 4))
                .status(BookingStatus.PENDING)
                .build();

        accommodation = Accommodation.builder()
                .id(300L)
                .dailyRate(BigDecimal.TEN)
                .availability(10)
                .build();
    }

    @Test
    void createSession_success() throws Exception {
        when(bookingRepo.findById(200L)).thenReturn(Optional.of(booking));
        when(accommodationRepo.findById(300L)).thenReturn(Optional.of(accommodation));

        // Utwórz pusty Session obiekt i ustaw jego pola przez refleksję
        Session mockSession = new Session();
        setField(mockSession, "id", "sess_mock");
        setField(mockSession, "url", "http://mock.url");

        try (MockedStatic<Session> mocked = mockStatic(Session.class)) {
            mocked.when(() -> Session.create(any(SessionCreateParams.class)))
                    .thenReturn(mockSession);

            Payment saved = Payment.builder()
                    .bookingId(200L)
                    .sessionId("sess_mock")
                    .sessionUrl("http://mock.url")
                    .status(PaymentStatus.PENDING)
                    .amountToPay(BigDecimal.valueOf(30))
                    .build();
            when(paymentRepo.save(any(Payment.class))).thenReturn(saved);

            Payment result = service.createSession(200L, "successUrl", "cancelUrl");

            assertEquals(200L, result.getBookingId());
            assertEquals("sess_mock", result.getSessionId());
            assertEquals("http://mock.url", result.getSessionUrl());
            assertEquals(PaymentStatus.PENDING, result.getStatus());
            assertEquals(BigDecimal.valueOf(30), result.getAmountToPay());
            verify(paymentRepo).save(any(Payment.class));
        }
    }

    // pomocnicza metoda do ustawiania prywatnych pól
    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void createSession_bookingNotFound_throws() {
        when(bookingRepo.findById(200L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> service.createSession(200L, "s", "c"));
        assertTrue(ex.getMessage().contains("Booking not found"));
    }

    @Test
    void createSession_accommodationNotFound_throws() {
        when(bookingRepo.findById(200L)).thenReturn(Optional.of(booking));
        when(accommodationRepo.findById(300L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> service.createSession(200L, "s", "c"));
        assertTrue(ex.getMessage().contains("Accommodation not found"));
    }

    @Test
    void markAsPaid_updatesStatus() {
        when(paymentRepo.findBySessionId("sess_123")).thenReturn(Optional.of(payment));
        when(bookingRepo.findById(200L)).thenReturn(Optional.of(booking));
        when(paymentRepo.save(payment)).thenReturn(payment);
        when(bookingRepo.save(booking)).thenReturn(booking);

        Payment paid = service.markAsPaid("sess_123");

        assertEquals(PaymentStatus.PAID, paid.getStatus());
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        verify(paymentRepo).save(payment);
        verify(bookingRepo).save(booking);
    }

    @Test
    void markAsPaid_unknownSession_throws() {
        when(paymentRepo.findBySessionId("x")).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> service.markAsPaid("x"));
        assertTrue(ex.getMessage().contains("Payment not found"));
    }

    @Test
    void markAsPaid_invalidStatus_throws() {
        payment.setStatus(PaymentStatus.PAID);
        when(paymentRepo.findBySessionId("sess_123")).thenReturn(Optional.of(payment));

        PaymentException ex = assertThrows(PaymentException.class,
                () -> service.markAsPaid("sess_123"));
        assertTrue(ex.getMessage().contains("Cannot mark payment as paid"));
    }

    @Test
    void findById_existing_returnsPayment() {
        when(paymentRepo.findById(5L)).thenReturn(Optional.of(payment));

        Payment found = service.findById(5L);
        assertEquals(payment, found);
    }

    @Test
    void findById_nonExisting_throws() {
        when(paymentRepo.findById(999L)).thenReturn(Optional.empty());
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> service.findById(999L));
        assertTrue(ex.getMessage().contains("Payment not found"));
    }

    @Test
    void findByBooking_returnsList() {
        when(paymentRepo.findByBookingId(200L)).thenReturn(List.of(payment));
        List<Payment> list = service.findByBooking(200L);
        assertEquals(1, list.size());
        assertEquals(200L, list.get(0).getBookingId());
    }

    @Test
    void findAll_returnsList() {
        when(paymentRepo.findAll()).thenReturn(List.of(payment));
        List<Payment> all = service.findAll();
        assertEquals(1, all.size());
        assertEquals(payment, all.get(0));
    }
}
