package com.challenge.vpp.service;

import com.challenge.vpp.dto.BatteryRequest;
import com.challenge.vpp.dto.BatteryStatisticsResponse;
import com.challenge.vpp.model.Battery;
import com.challenge.vpp.repo.BatteryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BatteryServiceImpl implements BatteryService{
    private final BatteryRepository batteryRepository;
    @Override
    @Transactional
    public void saveAll(List<BatteryRequest> batteryRequests) {
        List<Battery> batteries = batteryRequests.stream()
                .map(req -> Battery.builder()
                        .name(req.getName())
                        .postcode(req.getPostcode())
                        .wattCapacity(req.getCapacity())
                        .build())
                .collect(Collectors.toList());
        batteryRepository.saveAll(batteries);
    }

    @Override
    public BatteryStatisticsResponse getBatteriesInRange(int from, int to, Long minCapacity, Long maxCapacity) {
        List<Battery> batteries;

        if (minCapacity != null && maxCapacity != null) {
            batteries = batteryRepository.findByPostcodeBetweenAndWattCapacityBetween(from, to, minCapacity, maxCapacity);
        } else {
            batteries = batteryRepository.findByPostcodeBetween(from, to);
        }

        List<String> names = batteries.stream()
                .map(Battery::getName)
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());

        long total = batteries.stream().mapToLong(Battery::getWattCapacity).sum();
        double average = batteries.isEmpty() ? 0.0 :
                Math.round((double) total / batteries.size() * 100.0) / 100.0;
        log.info("Total watt capacity: {} & average {}" ,total, average);
        return BatteryStatisticsResponse.builder()
                .batteries(names)
                .totalWattCapacity(total)
                .averageWattCapacity(average)
                .build();
    }
}

