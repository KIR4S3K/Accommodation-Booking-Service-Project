package app.accommodationbookingservice.repository;

import app.accommodationbookingservice.model.Booking;
import app.accommodationbookingservice.model.enums.BookingStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(Long userId);

    List<Booking> findByStatus(String status);

    List<Booking> findByAccommodationIdAndStatusIn(
            Long accommodationId, List<BookingStatus> statuses);
}
