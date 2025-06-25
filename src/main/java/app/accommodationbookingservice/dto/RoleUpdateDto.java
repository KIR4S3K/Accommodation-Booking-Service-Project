package app.accommodationbookingservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class RoleUpdateDto {

    @NotBlank(message = "Role must not be blank")
    @Pattern(
            regexp = "USER|MANAGER|ADMIN",
            message = "Role must be one of: USER, MANAGER, ADMIN"
    )
    private String role;

    public RoleUpdateDto() {
    }

    public RoleUpdateDto(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
