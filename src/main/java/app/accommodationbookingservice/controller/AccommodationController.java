package app.accommodationbookingservice.controller;

import app.accommodationbookingservice.dto.AccommodationDto;
import app.accommodationbookingservice.dto.CreateAccommodationDto;
import app.accommodationbookingservice.dto.UpdateAccommodationDto;
import app.accommodationbookingservice.mapper.AccommodationMapper;
import app.accommodationbookingservice.model.Accommodation;
import app.accommodationbookingservice.service.AccommodationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Accommodations", description = "Manage accommodation inventory")
@RestController
@RequestMapping("/accommodations")
public class AccommodationController {

    private final AccommodationService service;
    private final AccommodationMapper mapper;

    public AccommodationController(AccommodationService service,
                                   AccommodationMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @Operation(summary = "Create a new accommodation (MANAGER only)")
    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<AccommodationDto> create(@Valid @RequestBody CreateAccommodationDto dto) {
        Accommodation saved = service.create(mapper.toEntity(dto));
        return ResponseEntity.ok(mapper.toDto(saved));
    }

    @Operation(summary = "List all accommodations")
    @GetMapping
    public ResponseEntity<List<AccommodationDto>> list() {
        List<AccommodationDto> dtos = service.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Get accommodation by ID")
    @GetMapping("/{id}")
    public ResponseEntity<AccommodationDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(mapper.toDto(service.findById(id)));
    }

    @Operation(summary = "Update accommodation (MANAGER only)")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<AccommodationDto> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAccommodationDto dto) {
        Accommodation entity = service.findById(id);
        mapper.updateEntityFromDto(dto, entity);
        return ResponseEntity.ok(mapper.toDto(service.update(id, entity)));
    }

    @Operation(summary = "Delete accommodation (MANAGER only)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            service.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
