package app.accommodationbookingservice.controller;

import app.accommodationbookingservice.dto.RoleUpdateDto;
import app.accommodationbookingservice.dto.UserDto;
import app.accommodationbookingservice.mapper.UserMapper;
import app.accommodationbookingservice.model.User;
import app.accommodationbookingservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Users", description = "User management endpoints")
@RestController
@RequestMapping("/users")
@Validated
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;

    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @Operation(summary = "Get current user profile")
    @GetMapping("/me")
    public ResponseEntity<UserDto> getProfile(Principal principal) {
        User current = userService.findByEmail(principal.getName());
        return ResponseEntity.ok(userMapper.toDto(current));
    }

    @Operation(summary = "Update current user profile")
    @PutMapping("/me")
    public ResponseEntity<UserDto> updateProfile(
            @Valid @RequestBody UserDto dto,
            Principal principal) {
        User current = userService.findByEmail(principal.getName());
        userMapper.updateEntityFromDto(dto, current);
        User updated = userService.save(current);
        return ResponseEntity.ok(userMapper.toDto(updated));
    }

    @Operation(summary = "Update user role (MANAGER only)")
    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<UserDto> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody RoleUpdateDto roleDto) {
        User updated = userService.updateRole(id, roleDto.getRole());
        return ResponseEntity.ok(userMapper.toDto(updated));
    }

    @Operation(summary = "List all users (MANAGER only)")
    @GetMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = userService.findAll().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }
}
