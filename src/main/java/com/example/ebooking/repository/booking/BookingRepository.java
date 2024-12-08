package com.example.ebooking.repository.booking;

import com.example.ebooking.model.Booking;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long>,
        JpaSpecificationExecutor<Booking> {
    Optional<List<Booking>> findByUserId(Long userId);

    Optional<Booking> findByUserIdAndId(Long userId, Long bookingId);

    Optional<List<Booking>> findByUserIdAndStatus(Long userId, Booking.Status status);

    List<Booking> findByAccommodationId(Long id);
}
