package app.accommodationbookingservice.dto;

import java.time.LocalDate;
import lombok.Data;

@Data
public class BookingDto {
    private Long id;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Long userId;
    private Long accommodationId;
    private String status;
}
