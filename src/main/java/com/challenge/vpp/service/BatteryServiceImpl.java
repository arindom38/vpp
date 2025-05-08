package com.challenge.vpp.service;

import com.challenge.vpp.dto.BatteryRequest;
import com.challenge.vpp.dto.BatteryStatisticsResponse;
import com.challenge.vpp.exception.BatteryDataException;
import com.challenge.vpp.exception.InvalidCapacityRangeException;
import com.challenge.vpp.exception.InvalidPostcodeRangeException;
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
        if (batteryRequests == null || batteryRequests.isEmpty()) {
            throw new BatteryDataException("Battery request list cannot be null or empty");
        }

        try {
            List<Battery> batteries = batteryRequests.stream()
                    .map(req -> Battery.builder()
                            .name(req.getName())
                            .postcode(req.getPostcode())
                            .wattCapacity(req.getCapacity())
                            .build())
                    .collect(Collectors.toList());
            batteryRepository.saveAll(batteries);
        } catch (Exception e) {
            throw new BatteryDataException("Error saving battery data: " + e.getMessage());
        }
    }

    @Override
    public BatteryStatisticsResponse getBatteriesInRange(int from, int to, Long minCapacity, Long maxCapacity) {
        // Validate postcode range
        if (from > to) {
            throw new InvalidPostcodeRangeException(
                "From postcode (" + from + ") must be less than or equal to to postcode (" + to + ")");
        }

        // Validate capacity range if both are provided
        if (minCapacity != null && maxCapacity != null && minCapacity > maxCapacity) {
            throw new InvalidCapacityRangeException(
                "Minimum capacity (" + minCapacity + ") must be less than or equal to maximum capacity (" + maxCapacity + ")");
        }

        List<Battery> batteries;
        try {
            if (minCapacity != null && maxCapacity != null) {
                batteries = batteryRepository.findByPostcodeBetweenAndWattCapacityBetween(from, to, minCapacity, maxCapacity);
            } else if (minCapacity != null) {
                batteries = batteryRepository.findByPostcodeBetweenAndWattCapacityGreaterThanEqual(from, to, minCapacity);
            } else if (maxCapacity != null) {
                batteries = batteryRepository.findByPostcodeBetweenAndWattCapacityLessThanEqual(from, to, maxCapacity);
            } else {
                batteries = batteryRepository.findByPostcodeBetween(from, to);
            }
        } catch (Exception e) {
            throw new BatteryDataException("Error retrieving battery data: " + e.getMessage());
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