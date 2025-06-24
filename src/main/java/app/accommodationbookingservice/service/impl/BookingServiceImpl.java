package app.accommodationbookingservice.service.impl;

import app.accommodationbookingservice.model.Accommodation;
import app.accommodationbookingservice.model.Booking;
import app.accommodationbookingservice.model.enums.BookingStatus;
import app.accommodationbookingservice.repository.AccommodationRepository;
import app.accommodationbookingservice.repository.BookingRepository;
import app.accommodationbookingservice.repository.PaymentRepository;
import app.accommodationbookingservice.service.BookingService;
import app.accommodationbookingservice.service.NotificationService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BookingServiceImpl implements BookingService {
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
        List<?> pendingPayments = paymentRepo.findPendingByUserId(b.getUserId());
        if (!pendingPayments.isEmpty()) {
            throw new IllegalStateException(
                    "You have pending payments; please complete them before booking.");
        }

        List<Booking> existing = repo.findByAccommodationIdAndStatusIn(
                b.getAccommodationId(),
                List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED)
        );
        boolean overlaps = existing.stream().anyMatch(ex ->
                !(b.getCheckOutDate().isBefore(ex.getCheckInDate())
                        || b.getCheckInDate().isAfter(ex.getCheckOutDate()))
        );
        if (overlaps) {
            throw new IllegalStateException(
                    "Accommodation " + b.getAccommodationId()
                            + " is already booked for these dates");
        }

        Accommodation acc = accommodationRepo.findById(b.getAccommodationId()).orElseThrow();
        if (acc.getAvailability() <= 0) {
            throw new IllegalStateException(
                    "No availability for accommodation " + b.getAccommodationId());
        }
        acc.setAvailability(acc.getAvailability() - 1);
        accommodationRepo.save(acc);

        b.setStatus(BookingStatus.PENDING);
        Booking saved = repo.save(b);
        notificationService.notify("New booking created: " + saved.getId());
        return saved;
    }

    @Override
    public Booking update(Long id, Booking b) {
        Booking ex = repo.findById(id).orElseThrow();
        ex.setCheckInDate(b.getCheckInDate());
        ex.setCheckOutDate(b.getCheckOutDate());
        Booking updated = repo.save(ex);
        notificationService.notify("Booking updated: " + updated.getId());
        return updated;
    }

    @Override
    public void cancel(Long id) {
        Booking ex = repo.findById(id).orElseThrow();
        if (ex.getStatus() == BookingStatus.CANCELED) {
            throw new IllegalStateException("Booking already canceled");
        }
        ex.setStatus(BookingStatus.CANCELED);
        repo.save(ex);

        Accommodation acc = accommodationRepo.findById(ex.getAccommodationId()).orElseThrow();
        acc.setAvailability(acc.getAvailability() + 1);
        accommodationRepo.save(acc);

        notificationService.notify("Booking canceled: " + ex.getId());
    }

    @Override
    public Booking findById(Long id) {
        return repo.findById(id).orElseThrow();
    }

    @Override
    public List<Booking> findByUser(Long userId) {
        return repo.findByUserId(userId);
    }

    @Override
    public List<Booking> findAll() {
        return repo.findAll();
    }
}
