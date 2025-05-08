package com.challenge.vpp.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class BatteryStatisticsResponse {
    private List<String> batteries;
    private long totalWattCapacity;
    private double averageWattCapacity;
}
