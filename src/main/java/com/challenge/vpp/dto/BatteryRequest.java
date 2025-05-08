package com.challenge.vpp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BatteryRequest {
    @NotBlank
    private String name;
    @NotBlank
    private int postcode;
    @NotBlank
    private long capacity;
}
