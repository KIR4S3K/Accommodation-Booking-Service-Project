package app.accommodationbookingservice.controller;

import app.accommodationbookingservice.model.Payment;
import app.accommodationbookingservice.model.User;
import app.accommodationbookingservice.model.enums.UserRole;
import app.accommodationbookingservice.service.BookingService;
import app.accommodationbookingservice.service.NotificationService;
import app.accommodationbookingservice.service.PaymentService;
import app.accommodationbookingservice.service.UserService;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentSvc;
    private final UserService userSvc;
    private final BookingService bookingSvc;
    private final NotificationService notificationService;

    public PaymentController(
            PaymentService paymentSvc,
            UserService userSvc,
            BookingService bookingSvc,
            NotificationService notificationService) {
        this.paymentSvc = paymentSvc;
        this.userSvc = userSvc;
        this.bookingSvc = bookingSvc;
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<Payment>> list(
            @RequestParam(required = false) Long bookingId,
            Principal principal) {

        User current = userSvc.findByEmail(principal.getName());

        if (current.getRole() == UserRole.MANAGER) {
            if (bookingId != null) {
                return ResponseEntity.ok(paymentSvc.findByBooking(bookingId));
            }
            return ResponseEntity.ok(paymentSvc.findAll());
        } else {
            if (bookingId == null) {
                throw new IllegalArgumentException("bookingId is required for customers");
            }
            List<Payment> own = paymentSvc.findByBooking(bookingId).stream()
                    .filter(p -> bookingSvc.findById(p.getBookingId())
                            .getUserId().equals(current.getId()))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(own);
        }
    }

    @PostMapping
    public ResponseEntity<Payment> createSession(
            @RequestParam Long bookingId,
            @RequestParam String success,
            @RequestParam String cancel) {

        return ResponseEntity.ok(
                paymentSvc.createSession(bookingId, success, cancel)
        );
    }

    @GetMapping("/success")
    public ResponseEntity<String> success(
            @RequestParam("session_id") String sessionId) {
        Payment paid = paymentSvc.markAsPaid(sessionId);
        notificationService.notify(
                "Payment successful for booking: " + paid.getBookingId()
        );
        return ResponseEntity.ok("Payment successful");
    }

    @GetMapping("/cancel")
    public ResponseEntity<String> cancel() {
        return ResponseEntity.ok("Payment cancelled, you can pay within 24h");
    }
}
