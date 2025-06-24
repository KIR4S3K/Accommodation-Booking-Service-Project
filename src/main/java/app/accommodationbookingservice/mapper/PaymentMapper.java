package app.accommodationbookingservice.mapper;

import app.accommodationbookingservice.dto.PaymentDto;
import app.accommodationbookingservice.model.Payment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    PaymentDto toDto(Payment entity);
}
