package app.accommodationbookingservice.scheduler;

import app.accommodationbookingservice.model.Booking;
import app.accommodationbookingservice.model.enums.BookingStatus;
import app.accommodationbookingservice.service.BookingService;
import app.accommodationbookingservice.service.NotificationService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ExpiredBookingScheduler {
    private final BookingService bookingSvc;
    private final NotificationService notif;

    public ExpiredBookingScheduler(BookingService bookingSvc,
                                   NotificationService notif) {
        this.bookingSvc = bookingSvc;
        this.notif = notif;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void checkExpired() {
        List<Booking> all = bookingSvc.findAll();
        boolean any = false;
        for (Booking b : all) {
            if (b.getStatus() == BookingStatus.CONFIRMED
                    && b.getCheckOutDate().isBefore(LocalDate.now()
                    .plusDays(1))) {
                b.setStatus(BookingStatus.EXPIRED);
                bookingSvc.update(b.getId(), b);
                notif.notify("Booking expired: " + b.getId());
                any = true;
            }
        }
        if (!any) {
            notif.notify("No expired bookings today!");
        }
    }
}
