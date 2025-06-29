package app.accommodationbookingservice.service;

import app.accommodationbookingservice.model.Accommodation;
import java.util.List;

public interface AccommodationService {
    Accommodation create(Accommodation a);

    Accommodation update(Long id, Accommodation a);

    void delete(Long id);

    Accommodation findById(Long id);

    List<Accommodation> findAll();
}
