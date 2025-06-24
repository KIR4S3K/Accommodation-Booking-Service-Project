package app.accommodationbookingservice.service;

import app.accommodationbookingservice.model.Payment;
import java.util.List;

public interface PaymentService {
    Payment createSession(Long bookingId, String successUrl, String cancelUrl);

    Payment markAsPaid(String sessionId);

    Payment findById(Long id);

    List<Payment> findByBooking(Long bookingId);

    List<Payment> findAll();
}
