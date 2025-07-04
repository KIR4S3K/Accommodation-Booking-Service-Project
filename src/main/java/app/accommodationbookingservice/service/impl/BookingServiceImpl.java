package app.accommodationbookingservice.service.impl;

import app.accommodationbookingservice.exception.AlreadyCanceledException;
import app.accommodationbookingservice.model.Accommodation;
import app.accommodationbookingservice.model.Booking;
import app.accommodationbookingservice.model.Payment;
import app.accommodationbookingservice.model.enums.BookingStatus;
import app.accommodationbookingservice.repository.AccommodationRepository;
import app.accommodationbookingservice.repository.BookingRepository;
import app.accommodationbookingservice.repository.PaymentRepository;
import app.accommodationbookingservice.service.BookingService;
import app.accommodationbookingservice.service.NotificationService;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class BookingServiceImpl implements BookingService {
    private static final Logger log = LoggerFactory.getLogger(BookingServiceImpl.class);

    private final BookingRepository repo;
    private final AccommodationRepository accommodationRepo;
    private final PaymentRepository paymentRepo;
    private final NotificationService notificationService;

    public BookingServiceImpl(
            BookingRepository repo,
            AccommodationRepository accommodationRepo,
            PaymentRepository paymentRepo,
            NotificationService notificationService) {
        this.repo = repo;
        this.accommodationRepo = accommodationRepo;
        this.paymentRepo = paymentRepo;
        this.notificationService = notificationService;
    }

    @Override
    public Booking create(Booking b) {
        List<Payment> pending = paymentRepo.findPendingByUserId(b.getUserId());
        if (!pending.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "You have pending payments; please complete them before booking."
            );
        }

        validateDateRange(b.getCheckInDate(), b.getCheckOutDate());

        boolean overlaps = repo.findByAccommodationIdAndStatusIn(
                        b.getAccommodationId(),
                        List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED)
                ).stream()
                .anyMatch(ex ->
                        !(b.getCheckOutDate().isBefore(ex.getCheckInDate().plusDays(1))
                                || b.getCheckInDate().isAfter(ex.getCheckOutDate().minusDays(1)))
                );
        if (overlaps) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Accommodation " + b.getAccommodationId() + " is already booked for these dates"
            );
        }

        Accommodation acc = accommodationRepo.findByIdForUpdate(b.getAccommodationId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Accommodation not found: " + b.getAccommodationId()
                ));
        if (acc.getAvailability() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No availability for accommodation " + b.getAccommodationId()
            );
        }

        acc.setAvailability(acc.getAvailability() - 1);
        accommodationRepo.save(acc);

        b.setStatus(BookingStatus.PENDING);
        Booking saved = repo.save(b);

        try {
            notificationService.notify("New booking created: " + saved.getId());
        } catch (Exception ex) {
            log.error("Notification failed for new booking {}", saved.getId(), ex);
        }

        return saved;
    }

    @Override
    public Booking update(Long id, Booking b) {
        Booking existing = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Booking not found: " + id
                ));

        validateDateRange(b.getCheckInDate(), b.getCheckOutDate());
        existing.setCheckInDate(b.getCheckInDate());
        existing.setCheckOutDate(b.getCheckOutDate());

        boolean overlaps = repo.findByAccommodationIdAndStatusIn(
                        existing.getAccommodationId(),
                        List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED)
                ).stream()
                .anyMatch(ex ->
                        !(existing.getCheckOutDate().isBefore(ex.getCheckInDate()
                                .plusDays(1))
                                || existing.getCheckInDate().isAfter(ex.getCheckOutDate()
                                .minusDays(1)))

                );
        if (overlaps) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Accommodation " + existing.getAccommodationId()
                            + " is already booked for these dates"
            );
        }

        Booking updated = repo.save(existing);
        try {
            notificationService.notify("Booking updated: " + updated.getId());
        } catch (Exception ex) {
            log.error("Notification failed for updated booking {}", updated.getId(), ex);
        }
        return updated;
    }

    @Override
    public void cancel(Long id) {
        Booking existing = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Booking not found: " + id
                ));

        if (existing.getStatus() == BookingStatus.CANCELED) {
            throw new AlreadyCanceledException("Booking already canceled: " + id);
        }

        existing.setStatus(BookingStatus.CANCELED);
        repo.save(existing);

        Accommodation acc = accommodationRepo.findByIdForUpdate(existing.getAccommodationId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Accommodation not found: " + existing.getAccommodationId()
                ));
        acc.setAvailability(acc.getAvailability() + 1);
        accommodationRepo.save(acc);

        try {
            notificationService.notify("Booking canceled: " + existing.getId());
        } catch (Exception ex) {
            log.error("Notification failed for canceled booking {}", existing.getId(), ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Booking findById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Booking not found: " + id
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> findByUser(Long userId) {
        return repo.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> findAll() {
        return repo.findAll();
    }

    private void validateDateRange(LocalDate in, LocalDate out) {
        if (in == null || out == null || !in.isBefore(out)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Check-in date must be before check-out date"
            );
        }
    }
}
