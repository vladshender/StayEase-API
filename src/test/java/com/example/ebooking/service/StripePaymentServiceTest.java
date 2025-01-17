package com.example.ebooking.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.ebooking.dto.booking.BookingResponseDto;
import com.example.ebooking.dto.payment.CreatePaymentSessionDto;
import com.example.ebooking.dto.payment.PaymentResponseDto;
import com.example.ebooking.dto.payment.PaymentWithoutSessionDto;
import com.example.ebooking.exception.exceptions.EntityNotFoundException;
import com.example.ebooking.exception.exceptions.PaymentStatusException;
import com.example.ebooking.mapper.BookingMapper;
import com.example.ebooking.mapper.PaymentMapper;
import com.example.ebooking.model.Accommodation;
import com.example.ebooking.model.Booking;
import com.example.ebooking.model.Payment;
import com.example.ebooking.repository.booking.BookingRepository;
import com.example.ebooking.repository.payment.PaymentRepository;
import com.example.ebooking.service.notification.NotificationService;
import com.example.ebooking.service.payment.StripePaymentService;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
public class StripePaymentServiceTest {
    public static final Long DEFAULT_ID_ONE = 1L;
    public static final int DEFAULT_TIMES = 1;
    
    public static final String SESSION_ID = "session_id_111";
    public static final String SESSION_URL = "https://stripe.com/session/123";
    @InjectMocks
    private StripePaymentService paymentService;

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private BookingMapper bookingMapper;
    @Mock
    private PaymentMapper paymentMapper;
    @Mock
    private NotificationService notificationService;
    @Mock
    private Session session;

    @Test
    @DisplayName("Returns all payments by user")
    void getPaymentsForUser_withValidUserId_returnPayments() {
        Payment payment = new Payment();
        payment.setId(DEFAULT_ID_ONE);
        payment.setStatus(Payment.PaymentStatus.PENDING);

        PaymentResponseDto responseDto = new PaymentResponseDto();
        responseDto.setId(payment.getId());
        responseDto.setStatus(payment.getStatus().toString());

        List<PaymentResponseDto> expected = List.of(responseDto);

        Pageable pageable = PageRequest.of(0, 10);
        List<Payment> payments = List.of(payment);
        Page<Payment> paymentPage = new PageImpl<>(payments, pageable, payments.size());

        when(paymentRepository.findByBookingUserId(DEFAULT_ID_ONE, pageable))
                .thenReturn(paymentPage);
        when(paymentMapper.toDtoList(payments)).thenReturn(expected);

        List<PaymentResponseDto> actual = paymentService.getPaymentsForUser(DEFAULT_ID_ONE,
                pageable);

        assertEquals(expected.size(), actual.size());

        verify(paymentRepository, times(DEFAULT_TIMES))
                .findByBookingUserId(DEFAULT_ID_ONE, pageable);
        verify(paymentMapper, times(DEFAULT_TIMES)).toDtoList(payments);
    }

    @Test
    @DisplayName("Returns payments with not exist user id")
    void getPaymentsForUser_withNotExistUserId_throwException() {
        Long userId = DEFAULT_ID_ONE;

        Pageable pageable = PageRequest.of(0, 10);
        List<Payment> payments = List.of();
        Page<Payment> paymentPage = new PageImpl<>(payments, pageable, payments.size());

        when(paymentRepository.findByBookingUserId(userId, pageable))
                .thenReturn(paymentPage);

        assertThatThrownBy(() -> paymentService.getPaymentsForUser(userId, pageable))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Payments not found by user id: "
                        + userId);

        verify(paymentRepository, times(DEFAULT_TIMES)).findByBookingUserId(userId, pageable);
    }

    @Test
    @DisplayName("Return all payment for admin")
    void getPaymentsForAdmin_withValidId_returnAllPayment() {
        Long userId = DEFAULT_ID_ONE;

        Payment firstPayment = new Payment();
        firstPayment.setId(DEFAULT_ID_ONE);
        firstPayment.setStatus(Payment.PaymentStatus.PENDING);

        Payment secondPayment = new Payment();
        secondPayment.setId(2L);
        secondPayment.setStatus(Payment.PaymentStatus.PENDING);

        PaymentResponseDto firstDto = new PaymentResponseDto();
        firstDto.setId(firstDto.getId());
        firstDto.setStatus(firstPayment.getStatus().toString());

        PaymentResponseDto secondDto = new PaymentResponseDto();
        secondDto.setId(secondDto.getId());
        secondDto.setStatus(secondDto.getStatus());

        List<PaymentResponseDto> expected = List.of(firstDto, secondDto);

        Pageable pageable = PageRequest.of(0, 10);
        List<Payment> payments = List.of(firstPayment, secondPayment);
        Page<Payment> paymentPage = new PageImpl<>(payments, pageable, payments.size());

        when(paymentRepository.findAll(pageable))
                .thenReturn(paymentPage);
        when(paymentMapper.toDtoList(payments)).thenReturn(expected);

        List<PaymentResponseDto> actual = paymentService.getPaymentsForAdmin(pageable);

        assertEquals(expected.size(), actual.size());

        verify(paymentRepository, times(DEFAULT_TIMES)).findAll(pageable);
        verify(paymentMapper, times(DEFAULT_TIMES)).toDtoList(payments);
    }

    @Test
    @DisplayName("Create session with not exist booking id")
    void createPaymentSession_withNotExistId_throwException() {
        Long bookingId = DEFAULT_ID_ONE;

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.createPaymentSession(bookingId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Payment not found by id: "
                        + bookingId);

        verify(bookingRepository, times(DEFAULT_TIMES)).findById(bookingId);
    }

    @Test
    @DisplayName("Create session with valid id")
    void createPaymentSessionDto_withValidId_returnDto() throws StripeException {
        Long bookingId = DEFAULT_ID_ONE;

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setAccommodation(new Accommodation());
        booking.getAccommodation().setDailyRate(BigDecimal.valueOf(100));
        booking.setCheckInDate(LocalDateTime.of(2025, 2, 23, 14, 0, 0));
        booking.setCheckOutDate(LocalDateTime.of(2025, 2, 24, 14, 0, 0));

        String sessionUrl = SESSION_URL;
        CreatePaymentSessionDto createPaymentSessionDto = new CreatePaymentSessionDto();
        createPaymentSessionDto.setSessionUrl(sessionUrl);

        String sessionId = SESSION_ID;
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(session.getId()).thenReturn(sessionId);
        when(session.getUrl()).thenReturn(sessionUrl);
        when(session.getExpiresAt()).thenReturn(System.currentTimeMillis() / 1000 + 3600);

        MockedStatic<Session> sessionMock = mockStatic(Session.class);
        sessionMock.when(() -> Session.create(any(SessionCreateParams.class)))
                .thenReturn(session);

        Payment payment = new Payment();
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(paymentMapper.toPaymentResponseDto(any(Payment.class)))
                .thenReturn(createPaymentSessionDto);

        CreatePaymentSessionDto expected = paymentService.createPaymentSession(bookingId);

        assertEquals(expected.getSessionUrl(), expected.getSessionUrl());

        sessionMock.close();

        verify(paymentRepository, times(DEFAULT_TIMES)).save(any(Payment.class));
        verify(paymentMapper, times(DEFAULT_TIMES)).toPaymentResponseDto(any(Payment.class));
    }

    @Test
    @DisplayName("Processing a successful payment with a valid id")
    void processSuccessfulPayment_withValidSessionId_returnDto() throws StripeException {
        Booking booking = new Booking();
        booking.setId(DEFAULT_ID_ONE);
        booking.setStatus(Booking.Status.PENDING);

        Payment payment = new Payment();
        payment.setId(DEFAULT_ID_ONE);
        payment.setSessionId(SESSION_ID);
        payment.setStatus(Payment.PaymentStatus.PAID);
        payment.setBooking(booking);

        PaymentWithoutSessionDto expected = new PaymentWithoutSessionDto(
                booking.getId(),
                Payment.PaymentStatus.PAID,
                BigDecimal.valueOf(100));

        when(paymentRepository.findBySessionId(anyString()))
                .thenReturn(Optional.of(payment));
        doNothing().when(paymentRepository).updateStatus(payment.getId(),
                Payment.PaymentStatus.PAID);
        doNothing().when(bookingRepository).updateStatus(booking.getId(),
                Booking.Status.CONFIRMED);
        when(paymentMapper.toPaymentWithoutSessionDto(payment)).thenReturn(expected);

        PaymentWithoutSessionDto actual = paymentService.processSuccessfulPayment(SESSION_ID);

        assertEquals(expected, actual);

        verify(notificationService, times(DEFAULT_TIMES))
                .sendPaymentSuccessMessage(payment);
        verify(paymentRepository, times(DEFAULT_TIMES)).findBySessionId(anyString());
        verify(paymentRepository, times(DEFAULT_TIMES))
                .updateStatus(payment.getId(), Payment.PaymentStatus.PAID);
        verify(bookingRepository, times(DEFAULT_TIMES))
                .updateStatus(booking.getId(), Booking.Status.CONFIRMED);
        verify(paymentMapper, times(DEFAULT_TIMES)).toPaymentWithoutSessionDto(payment);
    }

    @Test
    @DisplayName("Processing a canceled payment with a valid id")
    void processCancelPayment_withValidSessionId_returnDto() {
        Payment payment = new Payment();
        payment.setId(DEFAULT_ID_ONE);
        payment.setStatus(Payment.PaymentStatus.PAID);

        Booking booking = new Booking();
        booking.setId(DEFAULT_ID_ONE);
        booking.setStatus(Booking.Status.PENDING);
        booking.setAccommodation(new Accommodation());
        booking.getAccommodation().setId(DEFAULT_ID_ONE);
        payment.setBooking(booking);

        BookingResponseDto expected = new BookingResponseDto();
        expected.setId(DEFAULT_ID_ONE);
        expected.setStatus(Booking.Status.PENDING.toString());
        expected.setAccommodationId(DEFAULT_ID_ONE);

        String sessionId = SESSION_ID;

        when(paymentRepository.findBySessionId(sessionId)).thenReturn(Optional.of(payment));
        when(bookingMapper.toDto(booking)).thenReturn(expected);

        BookingResponseDto actual = paymentService.processCancelPayment(sessionId);

        assertEquals(expected, actual);

        verify(paymentRepository, times(DEFAULT_TIMES)).findBySessionId(sessionId);
        verify(bookingMapper, times(DEFAULT_TIMES)).toDto(booking);
    }

    @Test
    @DisplayName("Renew session with not expired status")
    void renewPaymentSession_withStatusNotExpired_throwException() {
        Long paymentId = DEFAULT_ID_ONE;

        Payment payment = new Payment();
        payment.setId(paymentId);
        payment.setStatus(Payment.PaymentStatus.PENDING);

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.renewPaymentSession(paymentId))
                .isInstanceOf(PaymentStatusException.class)
                .hasMessageContaining("Payment is not expired.");

        verify(paymentRepository, times(DEFAULT_TIMES)).findById(paymentId);
    }
}
