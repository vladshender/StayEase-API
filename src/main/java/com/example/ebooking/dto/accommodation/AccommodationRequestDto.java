package com.example.ebooking.dto.accommodation;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class AccommodationRequestDto {
    @NotBlank
    @Pattern(regexp = "HOUSE|APARTMENT|CONDO|VACATION_HOME",
            message = "Type must be one of HOUSE, APARTMENT, CONDO, VACATION_HOME")
    private String type;

    @NotBlank
    private String location;

    @NotBlank
    private String size;

    @NotEmpty
    private String[] amenities;

    @Min(value = 1)
    private BigDecimal dailyRate;

    @Min(value = 1)
    private Integer availability;
}
