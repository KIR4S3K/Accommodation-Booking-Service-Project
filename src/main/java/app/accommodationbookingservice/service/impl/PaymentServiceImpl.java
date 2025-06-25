package app.accommodationbookingservice.service.impl;

import app.accommodationbookingservice.exception.EntityNotFoundException;
import app.accommodationbookingservice.exception.PaymentException;
import app.accommodationbookingservice.model.Accommodation;
import app.accommodationbookingservice.model.Booking;
import app.accommodationbookingservice.model.Payment;
import app.accommodationbookingservice.model.enums.PaymentStatus;
import app.accommodationbookingservice.repository.AccommodationRepository;
import app.accommodationbookingservice.repository.BookingRepository;
import app.accommodationbookingservice.repository.PaymentRepository;
import app.accommodationbookingservice.service.PaymentService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepo;
    private final BookingRepository bookingRepo;
    private final AccommodationRepository accommodationRepo;
    private final String stripeKey;

    public PaymentServiceImpl(PaymentRepository paymentRepo,
                              BookingRepository bookingRepo,
                              AccommodationRepository accommodationRepo,
                              @org.springframework.beans.factory
                                      .annotation.Value("${stripe.secret-key}") String stripeKey) {
        this.paymentRepo = paymentRepo;
        this.bookingRepo = bookingRepo;
        this.accommodationRepo = accommodationRepo;
        this.stripeKey = stripeKey;
    }

    @Override
    public Payment createSession(Long bookingId, String successUrl, String cancelUrl) {
        Stripe.apiKey = stripeKey;

        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found: "
                        + bookingId));
        Accommodation acc = accommodationRepo.findById(booking.getAccommodationId())
                .orElseThrow(() -> new EntityNotFoundException("Accommodation not found: "
                        + booking.getAccommodationId()));
        long days = ChronoUnit.DAYS.between(booking.getCheckInDate(), booking
                .getCheckOutDate());
        BigDecimal amount = acc.getDailyRate().multiply(BigDecimal.valueOf(days));

        SessionCreateParams.LineItem.PriceData priceData =
                SessionCreateParams.LineItem.PriceData.builder()
                        .setCurrency("usd")
                        .setUnitAmount(amount.multiply(BigDecimal.valueOf(100)).longValue())
                        .setProductData(
                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName("Booking " + bookingId)
                                        .build()
                        )
                        .build();

        SessionCreateParams params =
                SessionCreateParams.builder()
                        .addLineItem(
                                SessionCreateParams.LineItem.builder()
                                        .setPriceData(priceData)
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
            return paymentRepo.save(p);
        } catch (StripeException e) {
            throw new PaymentException("Failed to create Stripe session", e);
        }
    }

    @Override
    public Payment markAsPaid(String sessionId) {
        Payment p = paymentRepo.findBySessionId(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found: "
                        + sessionId));
        if (p.getStatus() != PaymentStatus.PENDING) {
            throw new PaymentException("Cannot mark payment as paid in status: "
                    + p.getStatus());
        }
        p.setStatus(PaymentStatus.PAID);
        return paymentRepo.save(p);
    }

    @Override
    @Transactional(readOnly = true)
    public Payment findById(Long id) {
        return paymentRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found: "
                        + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> findByBooking(Long bookingId) {
        return paymentRepo.findByBookingId(bookingId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> findAll() {
        return paymentRepo.findAll();
    }
}
