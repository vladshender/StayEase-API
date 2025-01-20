package com.example.ebooking.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.ebooking.dto.accommodation.AccommodationRequestDto;
import com.example.ebooking.dto.accommodation.AccommodationResponseDto;
import com.example.ebooking.exception.exceptions.EntityNotFoundException;
import com.example.ebooking.mapper.AccommodationMapper;
import com.example.ebooking.model.Accommodation;
import com.example.ebooking.repository.accommodation.AccommodationRepository;
import com.example.ebooking.service.accommodation.AccommodationServiceImpl;
import com.example.ebooking.service.notification.TelegramNotificationService;
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

@ExtendWith(MockitoExtension.class)
public class AccommodationServiceTest {
    public static final Long DEFAULT_ID_ONE = 1L;
    public static final int DEFAULT_TIMES = 1;

    @InjectMocks
    private AccommodationServiceImpl accommodationService;

    @Mock
    private AccommodationRepository accommodationRepository;
    @Mock
    private AccommodationMapper accommodationMapper;
    @Mock
    private TelegramNotificationService notificationService;

    @Test
    @DisplayName("Returns list accommodation from database")
    void getAll_ValidPageable_returnAllAccommodation() {
        Accommodation accommodation = new Accommodation();
        accommodation.setId(DEFAULT_ID_ONE);
        accommodation.setType(Accommodation.Type.HOUSE);
        accommodation.setLocation("Lviv, Shevchenko street, 17");

        AccommodationResponseDto responseDto = new AccommodationResponseDto();
        responseDto.setId(accommodation.getId());
        responseDto.setType(accommodation.getType().toString());
        responseDto.setLocation(accommodation.getLocation());

        Pageable pageable = PageRequest.of(0, 10);
        List<Accommodation> expected = List.of(accommodation);
        Page<Accommodation> accommodationPage = new PageImpl<>(expected, pageable, expected.size());

        when(accommodationRepository.findAll(pageable)).thenReturn(accommodationPage);
        when(accommodationMapper.toListDto(expected)).thenReturn(List.of(responseDto));

        List<AccommodationResponseDto> actual = accommodationService.getAll(pageable);

        assertEquals(expected.size(), actual.size());

        verify(accommodationRepository, times(DEFAULT_TIMES)).findAll(pageable);
        verify(accommodationMapper, times(DEFAULT_TIMES)).toListDto(expected);
    }

    @Test
    @DisplayName("Returns accommodation by id with valid id")
    void getAccommodationById_withValidId_returnAccommodation() {
        Long accommodationId = DEFAULT_ID_ONE;
        Accommodation accommodation = new Accommodation();
        accommodation.setId(accommodationId);
        accommodation.setType(Accommodation.Type.HOUSE);
        accommodation.setLocation("Lviv, Shevchenko street, 17");

        AccommodationResponseDto expected = new AccommodationResponseDto();
        expected.setId(accommodation.getId());
        expected.setType(accommodation.getType().toString());
        expected.setLocation(accommodation.getLocation());

        when(accommodationRepository.findById(accommodationId))
                .thenReturn(Optional.of(accommodation));
        when(accommodationMapper.toDto(accommodation)).thenReturn(expected);

        AccommodationResponseDto actual = accommodationService
                .getAccommodationById(accommodationId);

        assertEquals(expected, actual);

        verify(accommodationRepository, times(DEFAULT_TIMES)).findById(accommodationId);
        verify(accommodationMapper, times(DEFAULT_TIMES)).toDto(accommodation);
    }

    @Test
    @DisplayName("Returns accommodation by id with not valid id")
    void getAccommodationById_withNotValidId_throwException() {
        Long accommodationId = DEFAULT_ID_ONE;

        when(accommodationRepository.findById(accommodationId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> accommodationService.getAccommodationById(accommodationId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Can`t find accommodation by id: "
                        + accommodationId);

        verify(accommodationRepository, times(DEFAULT_TIMES)).findById(accommodationId);
    }

    @Test
    @DisplayName("Save accommodation with valid request dto")
    void save_withValidRequestDto_returnAccommodation() {
        AccommodationRequestDto requestDto = new AccommodationRequestDto();
        requestDto.setType("HOUSE");

        Accommodation accommodation = new Accommodation();
        accommodation.setId(DEFAULT_ID_ONE);
        accommodation.setType(Accommodation.Type.HOUSE);

        AccommodationResponseDto expected = new AccommodationResponseDto();
        expected.setId(accommodation.getId());
        expected.setType("HOUSE");

        when(accommodationMapper.toModel(requestDto)).thenReturn(accommodation);
        when(accommodationRepository.save(accommodation)).thenReturn(accommodation);
        when(accommodationMapper.toDto(accommodation)).thenReturn(expected);

        AccommodationResponseDto actual = accommodationService.save(requestDto);

        assertEquals(expected, actual);

        verify(notificationService, times(DEFAULT_TIMES))
                .sendAccommodationCreateMessage(accommodation);
        verify(accommodationRepository, times(DEFAULT_TIMES)).save(accommodation);
        verify(accommodationMapper, times(DEFAULT_TIMES)).toDto(accommodation);
    }

    @Test
    @DisplayName("Update accommodation with valid request dto")
    void update_withValidDtoAndId_returnUpdatedAccommodation() {
        Long accommodationId = DEFAULT_ID_ONE;
        AccommodationRequestDto requestDto = new AccommodationRequestDto();
        requestDto.setLocation("Lviv, Shevchenko street, 23");

        Accommodation accommodation = new Accommodation();
        accommodation.setId(accommodationId);
        accommodation.setLocation("Lviv, Shevchenko street, 17");

        AccommodationResponseDto expected = new AccommodationResponseDto();
        expected.setLocation("Lviv, Shevchenko street, 23");
        expected.setId(accommodation.getId());

        when(accommodationRepository.findById(accommodationId))
                .thenReturn(Optional.of(accommodation));
        when(accommodationRepository.save(accommodation)).thenReturn(accommodation);
        when(accommodationMapper.toDto(accommodation)).thenReturn(expected);

        AccommodationResponseDto actual = accommodationService.update(requestDto, accommodationId);

        assertEquals(expected.getLocation(), actual.getLocation());

        verify(accommodationRepository, times(DEFAULT_TIMES)).findById(accommodationId);
        verify(accommodationRepository, times(DEFAULT_TIMES)).save(accommodation);
        verify(accommodationMapper, times(DEFAULT_TIMES)).toDto(accommodation);
    }

    @Test
    @DisplayName("Update accommodation with not valid accommodation id")
    void update_withNotValidId_throwException() {
        Long accommodationId = DEFAULT_ID_ONE;
        AccommodationRequestDto requestDto = new AccommodationRequestDto();
        requestDto.setLocation("Lviv, Shevchenko street, 23");

        when(accommodationRepository.findById(accommodationId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> accommodationService.update(requestDto, accommodationId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Can`t find accommodation by id: " + accommodationId);

        verify(accommodationRepository, times(DEFAULT_TIMES)).findById(accommodationId);
    }
}
