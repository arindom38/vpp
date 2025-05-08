package com.challenge.vpp.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BatteryRequestList {
    @NotEmpty(message = "Battery requests list cannot be empty")
    @Valid
    private List<BatteryRequest> batteries;
}