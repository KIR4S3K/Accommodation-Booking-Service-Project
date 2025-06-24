package app.accommodationbookingservice.dto;

import app.accommodationbookingservice.model.enums.UserRole;
import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private UserRole role;
}
