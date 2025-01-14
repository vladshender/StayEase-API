package com.example.ebooking.service;

import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.ebooking.bot.NotificationTelegramBot;
import com.example.ebooking.model.Accommodation;
import com.example.ebooking.model.Booking;
import com.example.ebooking.model.Payment;
import com.example.ebooking.model.User;
import com.example.ebooking.service.notification.NotificationTemplates;
import com.example.ebooking.service.notification.TelegramNotificationService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TelegramNotificationServiceTest {
    @InjectMocks
    private TelegramNotificationService telegramNotificationService;

    @Mock
    private NotificationTelegramBot telegramBot;

    @Test
    @DisplayName("Send notification when booking created")
    void sendBookingCreateMessage_withValidInputData_sendMessage() throws InterruptedException {
        Accommodation accommodation = new Accommodation();
        accommodation.setId(1L);
        accommodation.setType(Accommodation.Type.CONDO);

        User user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setCheckInDate(LocalDateTime.of(2024, 12, 30, 14, 0));
        booking.setCheckOutDate(LocalDateTime.of(2025, 1, 2, 11, 0));
        booking.setStatus(Booking.Status.PENDING);

        String messageExpected = String.format(
                NotificationTemplates.BOOKING_CREATED_TEMPLATE,
                booking.getId(),
                booking.getCheckInDate(),
                booking.getCheckOutDate(),
                accommodation.getId(),
                user.getId(),
                user.getFirstName() + " " + user.getLastName()
        );

        telegramNotificationService.sendBookingCreateMessage(accommodation, user, booking);

        Thread.sleep(500);

        verify(telegramBot, timeout(2000).times(1))
                .sendNotification(messageExpected);
    }

    @Test
    @DisplayName("Send notification when booking canceled")
    void sendBookingCanceledMessage_withValidInputData_sendMessage() throws
            ExecutionException,
            InterruptedException,
            TimeoutException {
        User user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setCheckInDate(LocalDateTime.of(2024, 12, 30, 14, 0));
        booking.setCheckOutDate(LocalDateTime.of(2025, 1, 2, 11, 0));
        booking.setStatus(Booking.Status.CANCELED);

        String expectedMessage = String.format(
                NotificationTemplates.BOOKING_CANCELED_TEMPLATE,
                booking.getId(),
                user.getId(),
                user.getFirstName() + " " + user.getLastName()
        );

        telegramNotificationService.sendBookingCanceledMessage(user, booking);

        Awaitility.await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(telegramBot, times(1)).sendNotification(expectedMessage);
        });
    }

    @Test
    @DisplayName("Send notification when new accommodation created")
    void sendAccommodationCreateMessage_withValidInputData_sendMessage() throws
            ExecutionException,
            InterruptedException,
            TimeoutException {
        Accommodation accommodation = new Accommodation();
        accommodation.setId(1L);
        accommodation.setType(Accommodation.Type.HOUSE);
        accommodation.setLocation("Kyiv");
        accommodation.setSize("55m");
        accommodation.setAmenities(Set.of(Accommodation.Amenities.WiFi));
        accommodation.setDailyRate(BigDecimal.valueOf(120));
        accommodation.setAvailability(2);

        String expectedMessage = String.format(
                NotificationTemplates.ACCOMMODATION_CREATED_MESSAGE,
                accommodation.getId(),
                accommodation.getType(),
                accommodation.getSize(),
                accommodation.getDailyRate()
        );

        telegramNotificationService.sendAccommodationCreateMessage(accommodation);

        Thread.sleep(500);

        verify(telegramBot, timeout(2000).times(1))
                .sendNotification(expectedMessage);
    }

    @Test
    @DisplayName("Send notification when accommodation released")
    void sendAccommodationReleaseMessage_withValidInputData_sendMessage() throws
            ExecutionException,
            InterruptedException,
            TimeoutException {
        List<Long> accommodationIds = List.of(1L, 2L);

        String expectedMessage = String.format(
                NotificationTemplates.ACCOMMODATION_RELEASE_MESSAGE,
                accommodationIds.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(", "))
        );

        telegramNotificationService.sendAccommodationReleaseMessage(accommodationIds);

        Thread.sleep(500);

        verify(telegramBot, timeout(2000).times(1))
                .sendNotification(expectedMessage);
    }

    @Test
    @DisplayName("Send notification when payment success")
    void sendPaymentSuccessMessage_withValidInputData_sendMessage() throws
            ExecutionException,
            InterruptedException,
            TimeoutException {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setBooking(new Booking());
        payment.getBooking().setId(2L);
        payment.setStatus(Payment.PaymentStatus.PAID);
        payment.setAmount(BigDecimal.valueOf(150));

        String expectedMessage = String.format(
                NotificationTemplates.PAYMENT_SUCCESS_MESSAGE,
                payment.getId(),
                payment.getBooking().getId(),
                payment.getStatus().toString(),
                payment.getAmount()
        );

        telegramNotificationService.sendPaymentSuccessMessage(payment);

        Thread.sleep(500);

        verify(telegramBot, timeout(2000).times(1))
                .sendNotification(expectedMessage);
    }
}

