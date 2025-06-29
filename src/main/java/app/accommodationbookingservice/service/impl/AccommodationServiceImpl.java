package app.accommodationbookingservice.service.impl;

import app.accommodationbookingservice.model.Accommodation;
import app.accommodationbookingservice.repository.AccommodationRepository;
import app.accommodationbookingservice.service.AccommodationService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AccommodationServiceImpl implements AccommodationService {
    private final AccommodationRepository repo;

    public AccommodationServiceImpl(AccommodationRepository repo) {
        this.repo = repo;
    }

    @Override
    public Accommodation create(Accommodation a) {
        return repo.save(a);
    }

    @Override
    public Accommodation update(Long id, Accommodation a) {
        Accommodation ex = repo.findById(id).orElseThrow();
        ex.setType(a.getType());
        ex.setLocation(a.getLocation());
        ex.setSize(a.getSize());
        ex.setAmenities(a.getAmenities());
        ex.setDailyRate(a.getDailyRate());
        ex.setAvailability(a.getAvailability());
        return repo.save(ex);
    }

    @Override
    public void delete(Long id) {
        repo.deleteById(id);
    }

    @Override
    public Accommodation findById(Long id) {
        return repo.findById(id).orElseThrow();
    }

    @Override
    public List<Accommodation> findAll() {
        return repo.findAll();
    }
}
