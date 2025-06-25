package app.accommodationbookingservice.mapper;

import app.accommodationbookingservice.dto.BookingDto;
import app.accommodationbookingservice.dto.CreateBookingDto;
import app.accommodationbookingservice.model.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    BookingDto toDto(Booking entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    Booking toEntity(CreateBookingDto dto);
}
