package app.accommodationbookingservice.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class UpdateAccommodationDto {
    private String location;
    private BigDecimal dailyRate;
    private Integer availability;
}
