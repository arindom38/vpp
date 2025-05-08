package com.challenge.vpp.service;

import com.challenge.vpp.dto.BatteryRequest;
import com.challenge.vpp.dto.BatteryStatisticsResponse;
import com.challenge.vpp.repo.BatteryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BatteryServiceImpl implements BatteryService{
    private final BatteryRepository batteryRepository;
    @Override
    public void saveAll(List<BatteryRequest> batteryRequests) {

    }

    @Override
    public BatteryStatisticsResponse getBatteriesInRange(int from, int to, Long minCapacity, Long maxCapacity) {
        return null;
    }
}
