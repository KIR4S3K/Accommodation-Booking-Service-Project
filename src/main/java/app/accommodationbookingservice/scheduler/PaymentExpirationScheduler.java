package app.accommodationbookingservice.scheduler;

import app.accommodationbookingservice.model.Payment;
import app.accommodationbookingservice.model.enums.PaymentStatus;
import app.accommodationbookingservice.repository.PaymentRepository;
import app.accommodationbookingservice.service.NotificationService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PaymentExpirationScheduler {
    private final PaymentRepository paymentRepo;
    private final NotificationService notif;

    public PaymentExpirationScheduler(PaymentRepository paymentRepo,
                                      NotificationService notif) {
        this.paymentRepo = paymentRepo;
        this.notif = notif;
    }

    @Scheduled(cron = "0 * * * * *")
    public void expireOldSessions() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        List<Payment> toExpire = paymentRepo.findAll().stream()
                .filter(p -> p.getStatus() == PaymentStatus.PENDING
                        && p.getCreatedAt().isBefore(cutoff))
                .toList();

        for (Payment p : toExpire) {
            p.setStatus(PaymentStatus.EXPIRED);
            paymentRepo.save(p);
            notif.notify("Payment session expired: " + p.getId());
        }
    }
}
