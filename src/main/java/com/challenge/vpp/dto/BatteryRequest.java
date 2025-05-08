package com.challenge.vpp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatteryRequest {
    private String name;
    private int postcode;
    private long wattCapacity;
}
