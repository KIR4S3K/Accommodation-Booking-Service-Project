package app.accommodationbookingservice.service;

import app.accommodationbookingservice.model.Booking;
import java.util.List;

public interface BookingService {
    Booking create(Booking b);

    Booking update(Long id, Booking b);

    void cancel(Long id);

    Booking findById(Long id);

    List<Booking> findByUser(Long userId);

    List<Booking> findAll();
}
