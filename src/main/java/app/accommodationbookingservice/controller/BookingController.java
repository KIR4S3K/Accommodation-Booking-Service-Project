package app.accommodationbookingservice.controller;

import app.accommodationbookingservice.model.Booking;
import app.accommodationbookingservice.service.BookingService;
import java.security.Principal;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bookings")
public class BookingController {
    private final BookingService svc;

    public BookingController(BookingService svc) {
        this.svc = svc;
    }

    @PostMapping
    public ResponseEntity<Booking> create(@RequestBody Booking b, Principal p) {
        return ResponseEntity.ok(svc.create(b));
    }

    @GetMapping("/my")
    public ResponseEntity<List<Booking>> my(Principal p) {
        return ResponseEntity.ok(svc.findByUser(1L));
    }

    @GetMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<Booking>> all(@RequestParam(required = false, name
                                                         = "user_id") Long userId,
                                             @RequestParam(required = false) String status) {
        if (userId != null) {
            return ResponseEntity.ok(svc.findByUser(userId));
        }
        if (status != null) {
            return ResponseEntity.ok(
                    svc.findAll().stream()
                            .filter(b -> b.getStatus().name().equalsIgnoreCase(status))
                            .toList()
            );
        }
        return ResponseEntity.ok(svc.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Booking> get(@PathVariable Long id) {
        return ResponseEntity
                .ok(svc.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Booking> update(@PathVariable Long id, @RequestBody Booking b) {
        return ResponseEntity.ok(svc.update(id, b));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        svc.cancel(id);
        return ResponseEntity.noContent().build();
    }
}
