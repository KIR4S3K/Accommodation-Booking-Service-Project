package app.accommodationbookingservice.mapper;

import app.accommodationbookingservice.dto.AccommodationDto;
import app.accommodationbookingservice.model.Accommodation;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccommodationMapper {
    AccommodationDto toDto(Accommodation entity);

    Accommodation toEntity(AccommodationDto dto);
}
