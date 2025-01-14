package com.example.ebooking.service.notification;

import com.example.ebooking.model.Accommodation;
import com.example.ebooking.model.Booking;
import com.example.ebooking.model.Payment;
import com.example.ebooking.model.User;
import java.util.List;

public interface NotificationService {

    void sendBookingCreateMessage(Accommodation accommodation,
                                  User user,
                                  Booking booking);

    void sendBookingCanceledMessage(User user, Booking booking);

    void sendAccommodationCreateMessage(Accommodation accommodation);

    void sendAccommodationReleaseMessage(List<Long> accommodationIds);

    void sendPaymentSuccessMessage(Payment payment);
}
