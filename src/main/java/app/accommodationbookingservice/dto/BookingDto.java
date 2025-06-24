package app.accommodationbookingservice.dto;

import app.accommodationbookingservice.model.enums.BookingStatus;
import java.time.LocalDate;
import lombok.Data;

@Data
public class BookingDto {
    private Long id;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Long accommodationId;
    private Long userId;
    private BookingStatus status;
}
