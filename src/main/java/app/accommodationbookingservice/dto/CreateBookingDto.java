package app.accommodationbookingservice.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Data;

@Data
public class CreateBookingDto {

    @NotNull(message = "Start date is required")
    @Future(message = "Start date must be in the future")
    private LocalDate checkInDate;

    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    private LocalDate checkOutDate;

    @NotNull(message = "Accommodation ID is required")
    private Long accommodationId;

    @AssertTrue(message = "Check-in date must be before check-out date")
    public boolean isValidDateRange() {
        if (checkInDate == null || checkOutDate == null) {
            return true;
        }
        return checkInDate.isBefore(checkOutDate);
    }
}
