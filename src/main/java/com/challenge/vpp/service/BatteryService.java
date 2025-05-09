package com.challenge.vpp.service;

import com.challenge.vpp.dto.BatteryRequest;
import com.challenge.vpp.dto.BatteryResponse;
import com.challenge.vpp.dto.BatteryStatisticsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BatteryService {
    void saveAll(List<BatteryRequest> batteryRequests);
    BatteryStatisticsResponse getBatteriesInRange(int from, int to, Long minCapacity, Long maxCapacity);

    BatteryResponse getBatteryById(Long id);

    void deleteBattery(Long id);

    BatteryResponse updateBattery(Long id, BatteryRequest batteryRequest);

    Page<BatteryResponse> getAllBatteries(Pageable pageable);
}
