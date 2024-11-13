package com.example.ebooking.dto.accommodation;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class AccommodationResponseDto {
    private Long id;
    private String type;
    private String location;
    private String size;
    private String[] amenities;
    private BigDecimal dailyRate;
    private Integer availability;
}
