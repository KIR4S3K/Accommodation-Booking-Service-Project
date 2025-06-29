package app.accommodationbookingservice.mapper;

import app.accommodationbookingservice.dto.AccommodationDto;
import app.accommodationbookingservice.dto.CreateAccommodationDto;
import app.accommodationbookingservice.dto.UpdateAccommodationDto;
import app.accommodationbookingservice.model.Accommodation;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface AccommodationMapper {
    AccommodationDto toDto(Accommodation entity);

    Accommodation toEntity(CreateAccommodationDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(UpdateAccommodationDto dto, @MappingTarget Accommodation entity);
}
