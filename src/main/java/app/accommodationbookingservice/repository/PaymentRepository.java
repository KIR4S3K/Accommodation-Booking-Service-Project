package app.accommodationbookingservice.repository;

import app.accommodationbookingservice.model.Payment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByBookingId(Long bookingId);

    Optional<Payment> findBySessionId(String sessionId);

    @Query("""
            select p from Payment p
            join Booking b on p.bookingId = b.id
            where b.userId = :userId and p.status = 'PENDING'
            """)
    List<Payment> findPendingByUserId(@Param("userId") Long userId);
}
