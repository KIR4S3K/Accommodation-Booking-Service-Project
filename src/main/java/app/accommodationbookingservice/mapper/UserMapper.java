package app.accommodationbookingservice.mapper;

import app.accommodationbookingservice.dto.UserDto;
import app.accommodationbookingservice.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User entity);
}
