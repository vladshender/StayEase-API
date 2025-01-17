package com.example.ebooking.service;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.ebooking.dto.booking.BookingFilterParameters;
import com.example.ebooking.dto.booking.BookingRequestDto;
import com.example.ebooking.dto.booking.BookingResponseDto;
import com.example.ebooking.exception.exceptions.BookingAvailabilityException;
import com.example.ebooking.exception.exceptions.EntityNotFoundException;
import com.example.ebooking.mapper.BookingMapper;
import com.example.ebooking.model.Accommodation;
import com.example.ebooking.model.Booking;
import com.example.ebooking.model.User;
import com.example.ebooking.repository.accommodation.AccommodationRepository;
import com.example.ebooking.repository.booking.BookingRepository;
import com.example.ebooking.repository.booking.BookingSpecificationBuilder;
import com.example.ebooking.repository.booking.spec.StatusSpecificationProvider;
import com.example.ebooking.repository.user.UserRepository;
import com.example.ebooking.service.booking.BookingServiceImpl;
import com.example.ebooking.service.notification.TelegramNotificationService;
import com.example.ebooking.service.payment.StripePaymentService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {
    public static final Long DEFAULT_ID_ONE = 1L;
    public static final int DEFAULT_TIMES = 1;
    
    @InjectMocks
    private BookingServiceImpl bookingService;

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private AccommodationRepository accommodationRepository;
    @Mock
    private BookingMapper bookingMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingSpecificationBuilder specificationBuilder;
    @Mock
    private TelegramNotificationService notificationService;
    @Mock
    private StripePaymentService paymentService;
    @Mock
    private StatusSpecificationProvider statusSpecificationProvider;

    @Test
    @DisplayName("Save booking with valid input data")
    void save_withValidInputData_returnResponseDto() {
        User user = new User();
        user.setId(DEFAULT_ID_ONE);
        user.setFirstName("Bob");
        user.setLastName("User");

        BookingRequestDto requestDto = new BookingRequestDto();
        requestDto.setAccommodationId(DEFAULT_ID_ONE);
        requestDto.setCheckInDate(LocalDateTime.of(2024, 12, 29, 12, 0, 0));
        requestDto.setCheckOutDate(LocalDateTime.of(2024, 12, 30, 14, 0, 0));

        Accommodation accommodation = new Accommodation();
        accommodation.setId(DEFAULT_ID_ONE);
        accommodation.setDailyRate(BigDecimal.valueOf(110));

        Booking booking = new Booking();
        booking.setId(DEFAULT_ID_ONE);
        booking.setAccommodation(accommodation);
        booking.setUser(user);
        booking.setStatus(Booking.Status.PENDING);

        BookingResponseDto expected = new BookingResponseDto();
        expected.setId(booking.getId());
        expected.setAccommodationId(accommodation.getId());
        expected.setStatus(booking.getStatus().toString());
        expected.setUserName(user.getFirstName());
        expected.setCheckInDate(booking.getCheckInDate());
        expected.setCheckOutDate(booking.getCheckOutDate());

        when(paymentService.existsByBookingUserIdAndStatus(user.getId()))
                .thenReturn(false);
        when(bookingRepository.findByAccommodationId(DEFAULT_ID_ONE)).thenReturn(List.of());
        when(accommodationRepository.findById(DEFAULT_ID_ONE))
                .thenReturn(Optional.of(accommodation));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(bookingMapper.toModel(any(BookingRequestDto.class))).thenReturn(booking);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingMapper.toDto(any(Booking.class))).thenReturn(expected);

        BookingResponseDto actual = bookingService.save(user, requestDto);

        assertEquals(expected.getAccommodationId(), actual.getAccommodationId());
        assertEquals(expected.getStatus(), actual.getStatus());
        assertEquals(expected.getCheckInDate(), actual.getCheckInDate());
        assertEquals(expected.getUserName(), actual.getUserName());

        verify(notificationService, times(DEFAULT_TIMES))
                .sendBookingCreateMessage(accommodation, user, booking);
        verify(paymentService, times(DEFAULT_TIMES))
                .existsByBookingUserIdAndStatus(user.getId());
        verify(bookingRepository, times(DEFAULT_TIMES)).findByAccommodationId(DEFAULT_ID_ONE);
        verify(accommodationRepository, times(DEFAULT_TIMES)).findById(DEFAULT_ID_ONE);
        verify(userRepository, times(DEFAULT_TIMES)).findById(DEFAULT_ID_ONE);
        verify(bookingMapper, times(DEFAULT_TIMES)).toModel(any(BookingRequestDto.class));
        verify(bookingRepository, times(DEFAULT_TIMES)).save(any(Booking.class));
        verify(bookingMapper, times(DEFAULT_TIMES)).toDto(any(Booking.class));
    }

    @Test
    @DisplayName("Save booking when exist overlapping booking`s date")
    void save_withOverlappingDate_throwException() {
        User user = new User();
        user.setId(DEFAULT_ID_ONE);
        user.setFirstName("Bob");
        user.setLastName("User");

        BookingRequestDto requestDto = new BookingRequestDto();
        requestDto.setAccommodationId(DEFAULT_ID_ONE);
        requestDto.setCheckInDate(LocalDateTime.of(2024, 12, 29, 12, 0, 0));
        requestDto.setCheckOutDate(LocalDateTime.of(2024, 12, 31, 14, 0, 0));

        Accommodation accommodation = new Accommodation();
        accommodation.setId(DEFAULT_ID_ONE);
        accommodation.setDailyRate(BigDecimal.valueOf(110));
        accommodation.setAvailability(1);

        Booking bookingFromDB = new Booking();
        bookingFromDB.setAccommodation(accommodation);
        bookingFromDB.setId(DEFAULT_ID_ONE);
        bookingFromDB.setCheckInDate(LocalDateTime.of(2024, 12, 28, 12, 0, 0));
        bookingFromDB.setCheckOutDate(LocalDateTime.of(2024, 12, 30, 12, 0, 0));

        when(paymentService.existsByBookingUserIdAndStatus(user.getId()))
                .thenReturn(false);
        when(bookingRepository.findByAccommodationId(DEFAULT_ID_ONE))
                .thenReturn(List.of(bookingFromDB));
        when(accommodationRepository.findById(DEFAULT_ID_ONE))
                .thenReturn(Optional.of(accommodation));

        assertThatThrownBy(() -> bookingService.save(user, requestDto))
                .isInstanceOf(BookingAvailabilityException.class)
                .hasMessageContaining(String.format("Accommodation is booked from %s to %s.",
                        bookingFromDB.getCheckInDate(), bookingFromDB.getCheckOutDate()));

        verify(paymentService, times(DEFAULT_TIMES)).existsByBookingUserIdAndStatus(user.getId());
        verify(bookingRepository, times(DEFAULT_TIMES)).findByAccommodationId(DEFAULT_ID_ONE);
        verify(accommodationRepository, times(DEFAULT_TIMES)).findById(DEFAULT_ID_ONE);
    }

    @Test
    @DisplayName("Returns all booking by user with valid user")
    void getAllBookingsByUser_withValidUser_returnListBookings() {
        User user = new User();
        user.setId(DEFAULT_ID_ONE);
        user.setFirstName("Alice");

        Booking booking = new Booking();
        booking.setId(DEFAULT_ID_ONE);
        booking.setAccommodation(new Accommodation());
        booking.getAccommodation().setId(DEFAULT_ID_ONE);
        booking.setUser(user);
        booking.setStatus(Booking.Status.PENDING);
        booking.setCheckInDate(LocalDateTime.of(2024, 12, 28, 12, 0, 0));
        booking.setCheckOutDate(LocalDateTime.of(2024, 12, 30, 12, 0, 0));

        BookingResponseDto responseDto = new BookingResponseDto();
        responseDto.setId(booking.getId());
        responseDto.setAccommodationId(booking.getAccommodation().getId());
        responseDto.setStatus(booking.getStatus().toString());
        responseDto.setUserName(user.getFirstName());
        responseDto.setCheckInDate(booking.getCheckInDate());
        responseDto.setCheckOutDate(booking.getCheckOutDate());
        List<BookingResponseDto> expected = List.of(responseDto);

        Pageable pageable = PageRequest.of(0, 10);
        List<Booking> bookings = List.of(booking);
        Page<Booking> bookingPage = new PageImpl<>(bookings, pageable, bookings.size());

        when(bookingRepository.findByUserId(user.getId(), pageable))
                .thenReturn(bookingPage);
        when(bookingMapper.toListDto(List.of(booking))).thenReturn(expected);

        List<BookingResponseDto> actual = bookingService.getAllBookingsByUser(user, pageable);

        assertEquals(expected, actual);

        verify(bookingRepository, times(DEFAULT_TIMES)).findByUserId(user.getId(), pageable);
        verify(bookingMapper, times(DEFAULT_TIMES)).toListDto(List.of(booking));
    }

    @Test
    @DisplayName("Returns all booking by user when bookings not exist")
    void getAllBookingsByUser_withValidUser_throwException() {
        User user = new User();
        user.setId(DEFAULT_ID_ONE);
        user.setFirstName("Alice");

        Pageable pageable = PageRequest.of(0, 10);
        List<Booking> bookings = List.of();
        Page<Booking> bookingPage = new PageImpl<>(bookings, pageable, bookings.size());

        when(bookingRepository.findByUserId(user.getId(), pageable))
                .thenReturn(bookingPage);

        assertThatThrownBy(() -> bookingService.getAllBookingsByUser(user, pageable))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Can`t find bookings "
                        + "by user id: " + user.getId());

        verify(bookingRepository, times(DEFAULT_TIMES)).findByUserId(user.getId(), pageable);
    }

    @Test
    @DisplayName("Returns booking by id for user with valid user and id")
    void getBookingByIdForUser_withValidUserAndId_returnResponseDto() {
        User user = new User();
        user.setId(DEFAULT_ID_ONE);
        user.setFirstName("Alice");

        Booking booking = new Booking();
        booking.setId(DEFAULT_ID_ONE);
        booking.setAccommodation(new Accommodation());
        booking.getAccommodation().setId(DEFAULT_ID_ONE);
        booking.setUser(user);
        booking.setStatus(Booking.Status.PENDING);
        booking.setCheckInDate(LocalDateTime.of(2024, 12, 28, 12, 0, 0));
        booking.setCheckOutDate(LocalDateTime.of(2024, 12, 30, 12, 0, 0));

        BookingResponseDto expected = new BookingResponseDto();
        expected.setId(booking.getId());
        expected.setAccommodationId(booking.getAccommodation().getId());
        expected.setStatus(booking.getStatus().toString());
        expected.setUserName(user.getFirstName());
        expected.setCheckInDate(booking.getCheckInDate());
        expected.setCheckOutDate(booking.getCheckOutDate());

        Long bookingId = DEFAULT_ID_ONE;

        when(bookingRepository.findByUserIdAndId(user.getId(), bookingId))
                .thenReturn(Optional.of(booking));
        when(bookingMapper.toDto(any(Booking.class))).thenReturn(expected);

        BookingResponseDto actual = bookingService.getBookingByIdForUser(user, bookingId);

        assertEquals(expected, actual);

        verify(bookingRepository, times(DEFAULT_TIMES)).findByUserIdAndId(user.getId(), bookingId);
        verify(bookingMapper, times(DEFAULT_TIMES)).toDto(any(Booking.class));
    }

    @Test
    @DisplayName("Returns booking by id for user when booking not exist")
    void getBookingByIdForUser_withValidUser_throwException() {
        User user = new User();
        user.setId(DEFAULT_ID_ONE);
        user.setFirstName("Alice");

        Long bookingId = DEFAULT_ID_ONE;

        when(bookingRepository.findByUserIdAndId(user.getId(), bookingId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getBookingByIdForUser(user, bookingId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Booking with id "
                        + bookingId
                        + " not found for user id: "
                        + user.getId());

        verify(bookingRepository, times(DEFAULT_TIMES)).findByUserIdAndId(user.getId(), bookingId);
    }

    @Test
    @DisplayName("Update booking with valid input data")
    void updateBookingByIdForAuthUser_withValidInputData_returnResponseDto() {
        User user = new User();
        user.setId(DEFAULT_ID_ONE);
        user.setFirstName("Bob");
        user.setLastName("User");

        BookingRequestDto requestDto = new BookingRequestDto();
        requestDto.setAccommodationId(DEFAULT_ID_ONE);
        requestDto.setCheckInDate(LocalDateTime.of(2024, 12, 29, 12, 0, 0));
        requestDto.setCheckOutDate(LocalDateTime.of(2025, 01, 01, 14, 0, 0));

        Accommodation accommodation = new Accommodation();
        accommodation.setId(DEFAULT_ID_ONE);
        accommodation.setDailyRate(BigDecimal.valueOf(110));

        Booking booking = new Booking();
        booking.setId(DEFAULT_ID_ONE);
        booking.setAccommodation(accommodation);
        booking.setUser(user);
        booking.setStatus(Booking.Status.PENDING);
        booking.setCheckInDate(LocalDateTime.of(2024, 12, 29, 12, 0, 0));
        booking.setCheckOutDate(LocalDateTime.of(2024, 12, 31, 12, 0, 0));

        BookingResponseDto expected = new BookingResponseDto();
        expected.setId(booking.getId());
        expected.setAccommodationId(accommodation.getId());
        expected.setStatus(booking.getStatus().toString());
        expected.setUserName(user.getFirstName());
        expected.setCheckInDate(requestDto.getCheckInDate());
        expected.setCheckOutDate(requestDto.getCheckOutDate());

        when(bookingRepository.findByUserIdAndId(user.getId(), booking.getId()))
                .thenReturn(Optional.of(booking));
        when(bookingRepository.findByAccommodationId(DEFAULT_ID_ONE)).thenReturn(List.of());
        doNothing().when(bookingMapper).updateBookingFromDto(requestDto, booking);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingMapper.toDto(any(Booking.class))).thenReturn(expected);

        BookingResponseDto actual = bookingService.updateBookingByIdForAuthUser(
                user,
                requestDto,
                booking.getId()
        );

        assertTrue(reflectionEquals(expected, actual, "id"));

        verify(bookingRepository, times(DEFAULT_TIMES))
                .findByUserIdAndId(user.getId(), booking.getId());
        verify(bookingRepository, times(DEFAULT_TIMES)).findByAccommodationId(DEFAULT_ID_ONE);
        verify(bookingMapper, times(DEFAULT_TIMES))
                .updateBookingFromDto(requestDto, booking);
        verify(bookingRepository, times(DEFAULT_TIMES)).save(any(Booking.class));
        verify(bookingMapper, times(DEFAULT_TIMES)).toDto(any(Booking.class));
    }

    @Test
    @DisplayName("Update booking when exist overlapping booking`s date")
    void updateBookingByIdForAuthUser_withOverlappingDate_throwException() {
        User user = new User();
        user.setId(DEFAULT_ID_ONE);
        user.setFirstName("Bob");
        user.setLastName("User");

        BookingRequestDto requestDto = new BookingRequestDto();
        requestDto.setAccommodationId(DEFAULT_ID_ONE);
        requestDto.setCheckInDate(LocalDateTime.of(2024, 12, 27, 12, 0, 0));
        requestDto.setCheckOutDate(LocalDateTime.of(2024, 12, 31, 12, 0, 0));

        Accommodation accommodation = new Accommodation();
        accommodation.setId(DEFAULT_ID_ONE);
        accommodation.setDailyRate(BigDecimal.valueOf(110));
        accommodation.setAvailability(1);

        Booking booking = new Booking();
        booking.setAccommodation(accommodation);
        booking.setId(2L);
        booking.setCheckInDate(LocalDateTime.of(2024, 12, 27, 12, 0, 0));
        booking.setCheckOutDate(LocalDateTime.of(2024, 12, 29, 12, 0, 0));

        Booking overlappingBooking = new Booking();
        overlappingBooking.setAccommodation(accommodation);
        overlappingBooking.setId(3L);
        overlappingBooking.setCheckInDate(LocalDateTime.of(2024, 12, 30, 12, 0, 0));
        overlappingBooking.setCheckOutDate(LocalDateTime.of(2025, 01, 01, 12, 0, 0));

        Long bookingId = DEFAULT_ID_ONE;

        when(bookingRepository.findByUserIdAndId(anyLong(), anyLong()))
                .thenReturn(Optional.of(booking));
        when(bookingRepository.findByAccommodationId(anyLong()))
                .thenReturn(List.of(overlappingBooking));
        when(accommodationRepository.findById(accommodation.getId()))
                .thenReturn(Optional.of(accommodation));

        assertThatThrownBy(() -> bookingService.updateBookingByIdForAuthUser(
                user,
                requestDto,
                bookingId
        ))
                .isInstanceOf(BookingAvailabilityException.class)
                .hasMessageContaining(String.format("Accommodation is booked from %s to %s.",
                        overlappingBooking.getCheckInDate(), overlappingBooking.getCheckOutDate()));

        verify(bookingRepository, times(DEFAULT_TIMES))
                .findByUserIdAndId(anyLong(), anyLong());
        verify(bookingRepository, times(DEFAULT_TIMES)).findByAccommodationId(anyLong());
        verify(accommodationRepository, times(DEFAULT_TIMES)).findById(accommodation.getId());
    }

    @Test
    @DisplayName("Returns all booking filter status or user id for admin")
    void getBookingByUserIdAndStatusForAdmin_withValidParameters_returnListBookings() {
        BookingResponseDto responseDto = new BookingResponseDto();
        responseDto.setId(DEFAULT_ID_ONE);
        responseDto.setStatus("PENDING");

        Booking booking = new Booking();
        booking.setId(responseDto.getId());
        booking.setStatus(Booking.Status.PENDING);

        String[] statusArray = new String[] {"PENDING"};
        String[] userIdArray = new String[0];
        BookingFilterParameters parameters = new BookingFilterParameters(statusArray,
                userIdArray);
        Specification<Booking> mockSpecification = mock(Specification.class);

        List<BookingResponseDto> expected = List.of(responseDto);

        Pageable pageable = PageRequest.of(0, 10);
        List<Booking> bookings = List.of(booking);
        Page<Booking> bookingPage = new PageImpl<>(bookings, pageable, bookings.size());

        when(specificationBuilder.build(parameters)).thenReturn(mockSpecification);
        when(bookingRepository.findAll(mockSpecification, pageable))
                .thenReturn(bookingPage);
        when(bookingMapper.toListDto(bookings)).thenReturn(expected);

        List<BookingResponseDto> actual = bookingService.getAllBookingByUserIdAndStatus(
                parameters,
                pageable
        );

        assertEquals(expected, actual);

        verify(specificationBuilder, times(DEFAULT_TIMES)).build(parameters);
        verify(bookingRepository, times(DEFAULT_TIMES)).findAll(mockSpecification, pageable);
        verify(bookingMapper, times(DEFAULT_TIMES)).toListDto(bookings);
    }
}
