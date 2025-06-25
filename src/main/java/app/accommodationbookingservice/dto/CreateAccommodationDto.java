package app.accommodationbookingservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class CreateAccommodationDto {

    @NotBlank(message = "Location must not be blank")
    private String location;

    @NotNull(message = "Daily rate is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Daily rate must be positive")
    private BigDecimal dailyRate;

    @NotNull(message = "Availability is required")
    @Min(value = 0, message = "Availability cannot be negative")
    private Integer availability;
}
