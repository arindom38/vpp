package com.challenge.vpp.service;

import com.challenge.vpp.dto.BatteryRequest;
import com.challenge.vpp.dto.BatteryStatisticsResponse;

import java.util.List;

public interface BatteryService {
    void saveAll(List<BatteryRequest> batteryRequests);
    BatteryStatisticsResponse getBatteriesInRange(int from, int to, Long minCapacity, Long maxCapacity);
}
