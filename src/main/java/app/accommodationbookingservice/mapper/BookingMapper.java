package app.accommodationbookingservice.mapper;

import app.accommodationbookingservice.dto.BookingDto;
import app.accommodationbookingservice.model.Booking;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BookingMapper {
    BookingDto toDto(Booking entity);

    Booking toEntity(BookingDto dto);
}
