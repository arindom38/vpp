package com.challenge.vpp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatteryStatisticsResponse {
    private List<String> batteries;
    private long totalWattCapacity;
    private double averageWattCapacity;
}
