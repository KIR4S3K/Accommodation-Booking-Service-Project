package app.accommodationbookingservice.scheduler;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.accommodationbookingservice.model.Booking;
import app.accommodationbookingservice.model.enums.BookingStatus;
import app.accommodationbookingservice.service.BookingService;
import app.accommodationbookingservice.service.NotificationService;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ExpiredBookingSchedulerTest {

    @Mock
    private BookingService bookingService;

    @Mock
    private NotificationService notif;

    @InjectMocks
    private ExpiredBookingScheduler scheduler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void whenNoExpired_thenNotifyNoExpired() {
        Booking b = Booking.builder()
                .id(1L)
                .status(BookingStatus.CONFIRMED)
                .checkOutDate(LocalDate.now().plusDays(5))
                .build();
        when(bookingService.findAll()).thenReturn(List.of(b));

        scheduler.checkExpired();

        verify(notif).notify("No expired bookings today!");
        verify(bookingService, never()).update(anyLong(), any());
    }
}
