package app.accommodationbookingservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.accommodationbookingservice.model.Payment;
import app.accommodationbookingservice.model.enums.PaymentStatus;
import app.accommodationbookingservice.repository.PaymentRepository;
import app.accommodationbookingservice.service.impl.PaymentServiceImpl;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository repo;

    @InjectMocks
    private PaymentServiceImpl service;

    private Payment payment;

    @BeforeEach
    void init() {
        payment = Payment.builder()
                .id(5L)
                .bookingId(200L)
                .status(PaymentStatus.PENDING)
                .sessionId("sess_123")
                .amountToPay(BigDecimal.TEN)
                .build();
    }

    @Test
    void markAsPaid_updatesStatus() {
        when(repo.findBySessionId("sess_123")).thenReturn(Optional.of(payment));
        when(repo.save(payment)).thenReturn(payment);

        Payment paid = service.markAsPaid("sess_123");

        assertEquals(PaymentStatus.PAID, paid.getStatus());
        verify(repo).save(payment);
    }

    @Test
    void markAsPaid_unknownId_throws() {
        when(repo.findBySessionId("x")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.markAsPaid("x"));

        assertTrue(ex.getMessage().contains("not found"));
    }

    @Test
    void findByBooking_returnsList() {
        when(repo.findByBookingId(200L)).thenReturn(List.of(payment));

        List<Payment> list = service.findByBooking(200L);

        assertEquals(1, list.size());
        assertEquals(200L, list.get(0).getBookingId());
    }
}
