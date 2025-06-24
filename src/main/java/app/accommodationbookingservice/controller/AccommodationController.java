package app.accommodationbookingservice.controller;

import app.accommodationbookingservice.model.Accommodation;
import app.accommodationbookingservice.service.AccommodationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
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
    private final AccommodationService svc;

    public AccommodationController(AccommodationService svc) {
        this.svc = svc;
    }

    @Operation(summary = "Create a new accommodation (MANAGER only)")
    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Accommodation> create(@RequestBody Accommodation a) {
        return ResponseEntity.ok(svc.create(a));
    }

    @Operation(summary = "List all accommodations")
    @GetMapping
    public ResponseEntity<List<Accommodation>> list() {
        return ResponseEntity.ok(svc.findAll());
    }

    @Operation(summary = "Get accommodation by ID")
    @GetMapping("/{id}")
    public ResponseEntity<Accommodation> get(@PathVariable Long id) {
        return ResponseEntity.ok(svc.findById(id));
    }

    @Operation(summary = "Update accommodation (MANAGER only)")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Accommodation> update(
            @PathVariable Long id,
            @RequestBody Accommodation a) {
        return ResponseEntity.ok(svc.update(id, a));
    }

    @Operation(summary = "Delete accommodation (MANAGER only)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        svc.delete(id);
        return ResponseEntity.noContent().build();
    }
}
