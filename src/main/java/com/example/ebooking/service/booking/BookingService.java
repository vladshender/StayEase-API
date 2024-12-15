package com.example.ebooking.service.booking;

import com.example.ebooking.dto.booking.BookingFilterParameters;
import com.example.ebooking.dto.booking.BookingResponseDto;
import com.example.ebooking.dto.booking.CreateAndUpdateBookingRequestDto;
import com.example.ebooking.dto.booking.UpdateBookingStatusRequestDto;
import com.example.ebooking.model.User;
import java.util.List;

public interface BookingService {
    BookingResponseDto save(User user, CreateAndUpdateBookingRequestDto requestDto);

    List<BookingResponseDto> getBookingsForAuthUser(User user);

    BookingResponseDto getBookingByIdFotAuthUser(User user, Long id);

    BookingResponseDto updateBookingByIdForAuthUser(User user,
                                                    CreateAndUpdateBookingRequestDto requestDto,
                                                    Long id);

    void canceledById(User user, Long id);

    void deleteById(User user, Long id);

    List<BookingResponseDto> getBookingByUserIdAndStatusForAdmin(
            BookingFilterParameters parameters);

    BookingResponseDto updateStatusById(UpdateBookingStatusRequestDto requestDto,
                                        Long id);

    void checkHourlyExpiredBookings();
}
