package app.accommodationbookingservice.controller;

import app.accommodationbookingservice.dto.MessageDto;
import app.accommodationbookingservice.model.Payment;
import app.accommodationbookingservice.model.User;
import app.accommodationbookingservice.model.enums.UserRole;
import app.accommodationbookingservice.service.BookingService;
import app.accommodationbookingservice.service.NotificationService;
import app.accommodationbookingservice.service.PaymentService;
import app.accommodationbookingservice.service.UserService;
import java.net.URI;
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

    private final PaymentService paymentService;
    private final UserService userService;
    private final BookingService bookingService;
    private final NotificationService notificationService;

    public PaymentController(
            PaymentService paymentService,
            UserService userService,
            BookingService bookingService,
            NotificationService notificationService) {
        this.paymentService = paymentService;
        this.userService = userService;
        this.bookingService = bookingService;
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<Payment>> list(
            @RequestParam(required = false) Long bookingId,
            Principal principal) {

        User current = userService.findByEmail(principal.getName());

        if (current.getRole() == UserRole.MANAGER) {
            if (bookingId != null) {
                return ResponseEntity.ok(paymentService.findByBooking(bookingId));
            }
            return ResponseEntity.ok(paymentService.findAll());
        } else {
            if (bookingId == null) {
                throw new IllegalArgumentException("bookingId is required for customers");
            }
            List<Payment> own = paymentService.findByBooking(bookingId).stream()
                    .filter(p -> bookingService.findById(p.getBookingId())
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

        Payment payment = paymentService.createSession(bookingId, success, cancel);
        URI location = URI.create("/payments/" + payment.getId());
        return ResponseEntity.created(location).body(payment);
    }

    @GetMapping("/success")
    public ResponseEntity<MessageDto> success(
            @RequestParam("session_id") String sessionId) {
        Payment paid = paymentService.markAsPaid(sessionId);
        notificationService.notify(
                "Payment successful for booking: " + paid.getBookingId()
        );
        return ResponseEntity.ok(new MessageDto("Payment successful"));
    }

    @GetMapping("/cancel")
    public ResponseEntity<MessageDto> cancel() {
        return ResponseEntity.ok(new MessageDto("Payment cancelled, you can pay within 24h"));
    }
}
