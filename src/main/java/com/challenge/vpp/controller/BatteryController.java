package com.challenge.vpp.controller;

import com.challenge.vpp.dto.BatteryRequest;
import com.challenge.vpp.dto.BatteryStatisticsResponse;
import com.challenge.vpp.service.BatteryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class BatteryController {
    private final BatteryService batteryService;

    @PostMapping("/batteries")
    public ResponseEntity<Void> addBatteries(@RequestBody List<BatteryRequest> batteryRequests) {
        batteryService.saveAll(batteryRequests);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/batteries")
    public ResponseEntity<BatteryStatisticsResponse> getBatteriesInRange(
            @RequestParam int from,
            @RequestParam int to,
            @RequestParam(required = false) Long minCapacity,
            @RequestParam(required = false) Long maxCapacity
    ) {
        return ResponseEntity.ok(batteryService.getBatteriesInRange(from, to, minCapacity, maxCapacity));
    }
}
