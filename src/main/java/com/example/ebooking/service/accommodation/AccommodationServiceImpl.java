package com.example.ebooking.service.accommodation;

import com.example.ebooking.dto.AccommodationRequestDto;
import com.example.ebooking.dto.AccommodationResponseDto;
import com.example.ebooking.exception.EntityNotFoundException;
import com.example.ebooking.mapper.AccommodationMapper;
import com.example.ebooking.model.Accommodation;
import com.example.ebooking.repository.AccommodationRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AccommodationServiceImpl implements AccommodationService {
    private final AccommodationRepository accommodationRepository;
    private final AccommodationMapper accommodationMapper;

    @Override
    public List<AccommodationResponseDto> getAll(Pageable pageable) {
        return accommodationMapper.toListDto(accommodationRepository.findAll(pageable)
                .getContent());
    }

    @Override
    public AccommodationResponseDto getAccommodationById(Long id) {
        Accommodation accommodation = accommodationRepository.findById(id)
                .orElseThrow(
                        () -> new EntityNotFoundException("Can`t find accommodation by id: " + id)
                );
        return accommodationMapper.toDto(accommodation);
    }

    @Override
    public AccommodationResponseDto save(AccommodationRequestDto requestDto) {
        Accommodation accommodation = accommodationMapper.toModel(requestDto);
        return accommodationMapper.toDto(accommodationRepository.save(accommodation));
    }

    @Transactional
    @Override
    public AccommodationResponseDto update(AccommodationRequestDto requestDto, Long id) {
        Accommodation accommodation = accommodationRepository.findById(id)
                .orElseThrow(
                        () -> new EntityNotFoundException("Can`t find accommodation by id" + id)
                );
        accommodationMapper.updateAccommodationFromDto(requestDto, accommodation);
        return accommodationMapper.toDto(accommodation);
    }

    @Transactional
    @Override
    public void deleteById(Long id) {
        accommodationRepository.deleteById(id);
    }
}
