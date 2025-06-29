package app.accommodationbookingservice.repository;

import app.accommodationbookingservice.model.Accommodation;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccommodationRepository extends JpaRepository<Accommodation, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Accommodation a WHERE a.id = :id")
    Optional<Accommodation> findByIdForUpdate(@Param("id") Long id);

}
