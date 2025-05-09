package com.challenge.vpp.controller;

import com.challenge.vpp.dto.BatteryRequest;
import com.challenge.vpp.dto.BatteryRequestList;
import com.challenge.vpp.dto.BatteryResponse;
import com.challenge.vpp.dto.BatteryStatisticsResponse;
import com.challenge.vpp.service.BatteryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class BatteryController {
    private final BatteryService batteryService;

    @PostMapping("/batteries")
    public ResponseEntity<Void> addBatteries(@Valid @RequestBody BatteryRequestList batteryRequests) {
        batteryService.saveAll(batteryRequests.getBatteries());
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

    @GetMapping(value = "/batteries/{id}")
    public ResponseEntity<BatteryResponse> getBatteryById(@PathVariable Long id) {
        return ResponseEntity.ok(batteryService.getBatteryById(id));
    }

    @DeleteMapping("/batteries/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteBattery(@PathVariable Long id) {
        batteryService.deleteBattery(id);
        return ResponseEntity.noContent().build();
    }


    @PutMapping(value = "/batteries/{id}")
    public ResponseEntity<BatteryResponse> updateBattery(
            @PathVariable Long id,
            @Valid @RequestBody BatteryRequest batteryRequest
    ) {
        return ResponseEntity.ok(batteryService.updateBattery(id, batteryRequest));
    }

    @GetMapping(value = "/batteries/all")
    public ResponseEntity<Page<BatteryResponse>> getAllBatteries(Pageable pageable) {
        return ResponseEntity.ok(batteryService.getAllBatteries(pageable));
    }



}
