package app.accommodationbookingservice.dto;

import app.accommodationbookingservice.model.enums.PaymentStatus;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class PaymentDto {
    private Long id;
    private PaymentStatus status;
    private Long bookingId;
    private String sessionUrl;
    private BigDecimal amountToPay;
}
