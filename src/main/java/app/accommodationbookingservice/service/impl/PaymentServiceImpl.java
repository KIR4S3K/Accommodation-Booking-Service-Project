package app.accommodationbookingservice.service.impl;

import app.accommodationbookingservice.model.Payment;
import app.accommodationbookingservice.model.enums.PaymentStatus;
import app.accommodationbookingservice.repository.PaymentRepository;
import app.accommodationbookingservice.service.PaymentService;
import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository repo;

    @Value("${stripe.secret-key}")
    private String stripeKey;

    public PaymentServiceImpl(PaymentRepository repo) {
        this.repo = repo;
    }

    @Override
    public Payment createSession(Long bookingId, String successUrl, String cancelUrl) {
        Stripe.apiKey = stripeKey;
        BigDecimal amount = BigDecimal.valueOf(1000);
        SessionCreateParams.LineItem.PriceData pd = SessionCreateParams.LineItem.PriceData.builder()
                .setCurrency("usd")
                .setUnitAmount(amount.multiply(BigDecimal.valueOf(100)).longValue())
                .setProductData(
                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                .setName("Booking " + bookingId)
                                .build()
                )
                .build();
        SessionCreateParams params = SessionCreateParams.builder()
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setPriceData(pd)
                                .setQuantity(1L)
                                .build()
                )
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .build();
        try {
            Session session = Session.create(params);
            Payment p = Payment.builder()
                    .bookingId(bookingId)
                    .sessionId(session.getId())
                    .sessionUrl(session.getUrl())
                    .status(PaymentStatus.PENDING)
                    .amountToPay(amount)
                    .build();
            return repo.save(p);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Stripe session", e);
        }
    }

    @Override
    public Payment markAsPaid(String sessionId) {
        Payment p = repo.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException(
                        "Payment with sessionId '" + sessionId + "' not found"));
        p.setStatus(PaymentStatus.PAID);
        return repo.save(p);
    }

    @Override
    public Payment findById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found: "
                        + id));
    }

    @Override
    public List<Payment> findByBooking(Long bookingId) {
        return repo.findByBookingId(bookingId);
    }

    @Override
    public List<Payment> findAll() {
        return repo.findAll();
    }
}
