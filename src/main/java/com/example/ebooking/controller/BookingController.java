package com.example.ebooking.controller;

import com.example.ebooking.dto.booking.BookingFilterParameters;
import com.example.ebooking.dto.booking.BookingResponseDto;
import com.example.ebooking.dto.booking.CreateAndUpdateBookingRequestDto;
import com.example.ebooking.dto.booking.UpdateBookingStatusRequestDto;
import com.example.ebooking.model.User;
import com.example.ebooking.service.booking.BookingService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bookings")
public class BookingController {
    public static final String USER_ROLES = "hasAuthority('ROLE_USER') "
            + "or hasAuthority('ROLE_GOLD_USER') "
            + "or hasAuthority('ROLE_PRIVILEGED_USER')";

    private final BookingService bookingService;

    @PreAuthorize(USER_ROLES)
    @PostMapping
    public BookingResponseDto save(
            Authentication authentication,
            @RequestBody @Valid CreateAndUpdateBookingRequestDto requestDto) {
        User user = (User) authentication.getPrincipal();
        return bookingService.save(user, requestDto);
    }

    @PreAuthorize(USER_ROLES)
    @GetMapping("/my")
    public List<BookingResponseDto> getAllBookingByAuthUser(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return bookingService.getBookingsForAuthUser(user);
    }

    @PreAuthorize(USER_ROLES)
    @GetMapping("/{id}")
    public BookingResponseDto getBookingByIdForAuthUser(Authentication authentication,
                                                        @PathVariable Long id) {
        User user = (User) authentication.getPrincipal();
        return bookingService.getBookingByIdFotAuthUser(user, id);
    }

    @PreAuthorize(USER_ROLES)
    @PutMapping("/{id}")
    public BookingResponseDto updateBookingByIdForAuthUser(
            Authentication authentication,
            @RequestBody CreateAndUpdateBookingRequestDto requestDto,
            @PathVariable Long id
    ) {
        User user = (User) authentication.getPrincipal();
        return bookingService.updateBookingByIdForAuthUser(user, requestDto, id);
    }

    @PreAuthorize(USER_ROLES)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBookingByIdForAuthUser(Authentication authentication,
                                              @PathVariable Long id) {
        User user = (User) authentication.getPrincipal();
        bookingService.deleteById(user, id);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping
    public List<BookingResponseDto> findBookingByUserIdAndStatus(
            BookingFilterParameters parameters) {
        return bookingService.getBookingByUserIdAndStatusForAdmin(parameters);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/statusArray/{id}")
    public BookingResponseDto updateStatusById(
            @RequestBody UpdateBookingStatusRequestDto requestDto,
            @PathVariable Long id) {
        return bookingService.updateStatusById(requestDto, id);
    }
}
