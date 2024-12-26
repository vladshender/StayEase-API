package com.example.ebooking.service.payment;

import com.example.ebooking.dto.booking.BookingResponseDto;
import com.example.ebooking.dto.payment.CreatePaymentSessionDto;
import com.example.ebooking.dto.payment.PaymentResponseDto;
import com.example.ebooking.dto.payment.PaymentWithoutSessionDto;
import com.stripe.exception.StripeException;
import java.util.List;

public interface PaymentService {
    List<PaymentResponseDto> getPaymentsForUser(Long userId);

    List<PaymentResponseDto> getPaymentsForAdmin();

    CreatePaymentSessionDto createPaymentSession(Long bookingId)
            throws StripeException;

    PaymentWithoutSessionDto processSuccessfulPayment(String sessionId)
            throws StripeException;

    BookingResponseDto processCancelPayment(String sessionId);

    void checkExpiredPayments() throws StripeException;

    CreatePaymentSessionDto renewPaymentSession(Long paymentId)
            throws StripeException;

    boolean existsByBookingUserIdAndStatus(Long userId);
}
