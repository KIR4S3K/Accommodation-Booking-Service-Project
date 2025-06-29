package app.accommodationbookingservice.controller;

import app.accommodationbookingservice.dto.BookingDto;
import app.accommodationbookingservice.dto.CreateBookingDto;
import app.accommodationbookingservice.mapper.BookingMapper;
import app.accommodationbookingservice.model.User;
import app.accommodationbookingservice.service.BookingService;
import app.accommodationbookingservice.service.UserService;
import jakarta.validation.Valid;
import java.security.Principal;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final UserService userService;
    private final BookingMapper bookingMapper;

    public BookingController(BookingService bookingService,
                             UserService userService, BookingMapper bookingMapper) {
        this.bookingService = bookingService;
        this.userService = userService;
        this.bookingMapper = bookingMapper;
    }

    @PostMapping
    public ResponseEntity<BookingDto> create(
            @Valid @RequestBody CreateBookingDto dto, Principal principal) {
        User user = userService.findByEmail(principal.getName());
        var booking = bookingMapper.toEntity(dto);
        booking.setUserId(user.getId());
        var saved = bookingService.create(booking);
        return ResponseEntity.ok(bookingMapper.toDto(saved));
    }

    @GetMapping("/my")
    public ResponseEntity<List<BookingDto>> my(Principal principal) {
        User user = userService.findByEmail(principal.getName());
        List<BookingDto> result = bookingService.findByUser(user.getId()).stream()
                .map(bookingMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<BookingDto>> all(
            @RequestParam(required = false, name = "user_id") Long userId,
            @RequestParam(required = false) String status) {
        var bookings = bookingService.findAll();

        if (userId != null) {
            bookings = bookingService.findByUser(userId);
        } else if (status != null) {
            bookings = bookings.stream()
                    .filter(b -> b.getStatus().name().equalsIgnoreCase(status))
                    .toList();
        }

        List<BookingDto> dtos = bookings.stream()
                .map(bookingMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(bookingMapper.toDto(bookingService.findById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookingDto> update(@PathVariable Long id,
                                             @Valid @RequestBody CreateBookingDto dto) {
        var updatedEntity = bookingMapper.toEntity(dto);
        var updated = bookingService.update(id, updatedEntity);
        return ResponseEntity.ok(bookingMapper.toDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        bookingService.cancel(id);
        return ResponseEntity.noContent().build();
    }
}
