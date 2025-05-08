package com.challenge.vpp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BatteryRequest {
    @NotBlank(message = "Battery name cannot be blank")
    private String name;
    
    @NotNull(message = "Postcode is required")
    @Positive(message = "Postcode must be a positive number")
    private Integer postcode;
    
    @NotNull(message = "Capacity is required")
    @Positive(message = "Capacity must be a positive number")
    private Long capacity;
}