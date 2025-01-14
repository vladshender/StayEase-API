package com.example.ebooking.service.notification;

import com.example.ebooking.bot.NotificationTelegramBot;
import com.example.ebooking.model.Accommodation;
import com.example.ebooking.model.Booking;
import com.example.ebooking.model.Payment;
import com.example.ebooking.model.User;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TelegramNotificationService implements NotificationService {
    private final NotificationTelegramBot telegramBot;

    @Async
    @Override
    public void sendBookingCreateMessage(Accommodation accommodation, User user,
                                                            Booking booking) {
        String message = String.format(
                NotificationTemplates.BOOKING_CREATED_TEMPLATE,
                booking.getId(),
                booking.getCheckInDate(),
                booking.getCheckOutDate(),
                accommodation.getId(),
                user.getId(),
                getUserName(user)
        );

        telegramBot.sendNotification(message);
    }

    @Async
    @Override
    public void sendBookingCanceledMessage(User user,
                                            Booking booking) {
        String message = String.format(
                NotificationTemplates.BOOKING_CANCELED_TEMPLATE,
                booking.getId(),
                user.getId(),
                getUserName(user)
        );
        telegramBot.sendNotification(message);
    }

    @Async
    @Override
    public void sendAccommodationCreateMessage(Accommodation accommodation) {
        String message = String.format(
                NotificationTemplates.ACCOMMODATION_CREATED_MESSAGE,
                accommodation.getId(),
                accommodation.getType(),
                accommodation.getSize(),
                accommodation.getDailyRate()
        );

        telegramBot.sendNotification(message);
    }

    @Async
    @Override
    public void sendAccommodationReleaseMessage(List<Long> accommodationIds) {
        String message = String.format(
                NotificationTemplates.ACCOMMODATION_RELEASE_MESSAGE,
                accommodationIds.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(", ")));

        telegramBot.sendNotification(message);
    }

    @Async
    @Override
    public void sendPaymentSuccessMessage(Payment payment) {
        String message = String.format(
                NotificationTemplates.PAYMENT_SUCCESS_MESSAGE,
                payment.getId(),
                payment.getBooking().getId(),
                payment.getStatus().toString(),
                payment.getAmount()
        );
        telegramBot.sendNotification(message);
    }

    private String getUserName(User user) {
        return user.getFirstName() + " " + user.getLastName();
    }
}
