package com.challenge.vpp.controller;

import com.challenge.vpp.dto.BatteryRequest;
import com.challenge.vpp.dto.BatteryStatisticsResponse;
import com.challenge.vpp.service.BatteryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/batteries")
@RequiredArgsConstructor
public class BatteryController {
    private final BatteryService batteryService;

    @PostMapping
    public ResponseEntity<Void> addBatteries(@RequestBody List<BatteryRequest> batteryRequests) {
        batteryService.saveAll(batteryRequests);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<BatteryStatisticsResponse> getBatteriesInRange(
            @RequestParam int from,
            @RequestParam int to,
            @RequestParam(required = false) Long minCapacity,
            @RequestParam(required = false) Long maxCapacity
    ) {
        return ResponseEntity.ok(batteryService.getBatteriesInRange(from, to, minCapacity, maxCapacity));
    }
}
