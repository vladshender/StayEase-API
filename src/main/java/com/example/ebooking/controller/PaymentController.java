package com.example.ebooking.controller;

import com.example.ebooking.dto.booking.BookingResponseDto;
import com.example.ebooking.dto.payment.CreatePaymentSessionDto;
import com.example.ebooking.dto.payment.PaymentResponseDto;
import com.example.ebooking.dto.payment.PaymentWithoutSessionDto;
import com.example.ebooking.service.payment.StripePaymentService;
import com.stripe.exception.StripeException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {
    public static final String USER_ROLES = "hasAuthority('ROLE_USER') "
            + "or hasAuthority('ROLE_GOLD_USER') "
            + "or hasAuthority('ROLE_PRIVILEGED_USER')";

    private final StripePaymentService paymentService;

    @GetMapping("/user")
    @PreAuthorize(USER_ROLES)
    public List<PaymentResponseDto> getPaymentsByUser() {
        return paymentService.getPaymentsForAdmin();
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public List<PaymentResponseDto> getPaymentsByUser(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return paymentService.getPaymentsForUser(userId);
    }

    @PostMapping("/session")
    @PreAuthorize(USER_ROLES)
    public CreatePaymentSessionDto createPaymentSession(@RequestParam Long bookingId)
            throws StripeException {
        return paymentService.createPaymentSession(bookingId);
    }

    @GetMapping("/success")
    @PreAuthorize(USER_ROLES)
    public PaymentWithoutSessionDto handleSuccessPayment(@RequestParam String sessionId)
            throws StripeException {
        return paymentService.processSuccessfulPayment(sessionId);
    }

    @GetMapping("/cancel")
    @PreAuthorize(USER_ROLES)
    public BookingResponseDto handleCancelledPayment(@RequestParam String sessionId) {
        return paymentService.processCancelPayment(sessionId);
    }

    @PreAuthorize(USER_ROLES)
    @PostMapping("/{paymentId}/renew")
    public CreatePaymentSessionDto renewPaymentSession(@PathVariable Long paymentId)
            throws StripeException {
        return paymentService.renewPaymentSession(paymentId);
    }
}
