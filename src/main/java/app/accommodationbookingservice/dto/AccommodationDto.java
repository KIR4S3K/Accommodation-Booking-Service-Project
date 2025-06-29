package app.accommodationbookingservice.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class AccommodationDto {
    private Long id;
    private String location;
    private BigDecimal dailyRate;
    private Integer availability;
}
