package com.challenge.vpp.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BatteryResponse {
    private Long id;
    private String name;
    private Integer postcode;
    private Long wattCapacity;
}