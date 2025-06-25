package app.accommodationbookingservice.dto;

import app.accommodationbookingservice.model.enums.UserRole;
import lombok.Data;

@Data
public class RegisterRequestDto {
    private String email;
    private String firstName;
    private String lastName;
    private String password;
    private UserRole role;
}
