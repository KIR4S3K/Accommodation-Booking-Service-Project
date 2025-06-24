package app.accommodationbookingservice.repository;

import app.accommodationbookingservice.model.Accommodation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccommodationRepository extends JpaRepository<Accommodation, Long> {
}
