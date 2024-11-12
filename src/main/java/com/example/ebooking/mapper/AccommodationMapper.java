package com.example.ebooking.mapper;

import com.example.ebooking.config.MapperConfig;
import com.example.ebooking.dto.AccommodationRequestDto;
import com.example.ebooking.dto.AccommodationResponseDto;
import com.example.ebooking.model.Accommodation;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfig.class)
public interface AccommodationMapper {
    Accommodation toModel(AccommodationRequestDto requestDto);

    AccommodationResponseDto toDto(Accommodation accommodation);

    List<AccommodationResponseDto> toListDto(List<Accommodation> accommodationList);

    void updateAccommodationFromDto(AccommodationRequestDto requestDto,
                                    @MappingTarget Accommodation accommodation);
}
