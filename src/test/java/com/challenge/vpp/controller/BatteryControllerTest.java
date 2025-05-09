package com.challenge.vpp.controller;

import com.challenge.vpp.dto.BatteryRequest;
import com.challenge.vpp.dto.BatteryRequestList;
import com.challenge.vpp.dto.BatteryResponse;
import com.challenge.vpp.dto.BatteryStatisticsResponse;
import com.challenge.vpp.exception.BatteryDataException;
import com.challenge.vpp.exception.InvalidCapacityRangeException;
import com.challenge.vpp.exception.InvalidPostcodeRangeException;
import com.challenge.vpp.service.BatteryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BatteryController.class)
class BatteryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BatteryService batteryService;

    @Autowired
    private ObjectMapper objectMapper;

    private BatteryRequest batteryRequest;
    private BatteryStatisticsResponse statisticsResponse;

    @BeforeEach
    void setUp() {
        batteryRequest = BatteryRequest.builder()
                .name("Test Battery")
                .postcode(2000)
                .capacity(100L)
                .build();

        statisticsResponse = BatteryStatisticsResponse.builder()
                .batteries(List.of("Battery1", "Battery2"))
                .totalWattCapacity(300L)
                .averageWattCapacity(150.0)
                .build();
    }

    @Test
    void addBatteries_WithValidRequest_ShouldReturnOk() throws Exception {
        BatteryRequestList requestList = new BatteryRequestList();
        requestList.setBatteries(List.of(batteryRequest));

        performPostRequest(requestList)
                .andExpect(status().isOk());

        verify(batteryService).saveAll(requestList.getBatteries());
    }

    @Test
    void addBatteries_WithEmptyList_ShouldReturnBadRequest() throws Exception {
        BatteryRequestList requestList = new BatteryRequestList();
        requestList.setBatteries(Collections.emptyList());

        performPostRequest(requestList)
                .andExpect(status().isBadRequest());
    }

    @Test
    void addBatteries_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        BatteryRequest invalidRequest = BatteryRequest.builder().build();
        BatteryRequestList requestList = new BatteryRequestList();
        requestList.setBatteries(List.of(invalidRequest));

        performPostRequest(requestList)
                .andExpect(status().isBadRequest());
    }

    @Test
    void addBatteries_WhenServiceThrowsException_ShouldReturnBadRequest() throws Exception {
        BatteryRequestList requestList = new BatteryRequestList();
        requestList.setBatteries(List.of(batteryRequest));

        doThrow(new BatteryDataException("Error saving batteries"))
                .when(batteryService).saveAll(any());

        performPostRequest(requestList)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error saving batteries"));
    }

    @ParameterizedTest
    @MethodSource("provideInvalidRequests")
    void addBatteries_WithInvalidRequests_ShouldReturnBadRequest(BatteryRequestList invalidRequests) throws Exception {
        performPostRequest(invalidRequests)
                .andExpect(status().isBadRequest());
    }

    private static Stream<Arguments> provideInvalidRequests() {
        return Stream.of(
                Arguments.of(new BatteryRequestList(Collections.singletonList(
                        BatteryRequest.builder().build()))),
                Arguments.of(new BatteryRequestList(Collections.singletonList(
                        BatteryRequest.builder()
                                .name("")
                                .postcode(-1)
                                .capacity(-1L)
                                .build()))),
                Arguments.of(new BatteryRequestList(Collections.singletonList(
                        BatteryRequest.builder()
                                .name(null)
                                .postcode(null)
                                .capacity(null)
                                .build())))
        );
    }

    @Test
    void getBatteriesInRange_WithValidParameters_ShouldReturnSuccess() throws Exception {
        when(batteryService.getBatteriesInRange(anyInt(), anyInt(), anyLong(), anyLong()))
                .thenReturn(statisticsResponse);

        performGetRequest(2000, 2100, 50L, 200L)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.batteries").isArray())
                .andExpect(jsonPath("$.batteries", hasSize(2)))
                .andExpect(jsonPath("$.totalWattCapacity").value(300))
                .andExpect(jsonPath("$.averageWattCapacity").value(150.0));
    }

    @Test
    void getBatteriesInRange_WithoutOptionalParameters_ShouldReturnSuccess() throws Exception {
        when(batteryService.getBatteriesInRange(anyInt(), anyInt(), any(), any()))
                .thenReturn(statisticsResponse);

        performGetRequest(2000, 2100, null, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.batteries", hasSize(2)));
    }

    @Test
    void getBatteriesInRange_WhenServiceThrowsInvalidPostcodeRange_ShouldReturnBadRequest() throws Exception {
        when(batteryService.getBatteriesInRange(anyInt(), anyInt(), any(), any()))
                .thenThrow(new InvalidPostcodeRangeException("Invalid postcode range"));

        performGetRequest(2100, 2000, null, null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid postcode range"));
    }

    @Test
    void getBatteriesInRange_WhenServiceThrowsInvalidCapacityRange_ShouldReturnBadRequest() throws Exception {
        when(batteryService.getBatteriesInRange(anyInt(), anyInt(), anyLong(), anyLong()))
                .thenThrow(new InvalidCapacityRangeException("Invalid capacity range"));

        performGetRequest(2000, 2100, 200L, 100L)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid capacity range"));
    }

    @Test
    void getBatteriesInRange_WithMissingRequiredParameters_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/batteries")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBatteriesInRange_WithInvalidParameterTypes_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/batteries")
                        .param("from", "invalid")
                        .param("to", "invalid")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    private ResultActions performPostRequest(BatteryRequestList request) throws Exception {
        return mockMvc.perform(post("/api/v1/batteries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print());
    }

    private ResultActions performGetRequest(Integer from, Integer to, Long minCapacity, Long maxCapacity) throws Exception {
        return mockMvc.perform(get("/api/v1/batteries")
                        .param("from", String.valueOf(from))
                        .param("to", String.valueOf(to))
                        .param("minCapacity", minCapacity != null ? String.valueOf(minCapacity) : null)
                        .param("maxCapacity", maxCapacity != null ? String.valueOf(maxCapacity) : null)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());
    }

    @Test
    void getBatteryById_WithValidId_ShouldReturnBatteryResponse() throws Exception {
        BatteryResponse response = BatteryResponse.builder()
                .id(1L)
                .name("Battery1")
                .postcode(2000)
                .wattCapacity(150L)
                .build();

        when(batteryService.getBatteryById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/batteries/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Battery1"))
                .andExpect(jsonPath("$.postcode").value(2000))
                .andExpect(jsonPath("$.wattCapacity").value(150));
    }

    @Test
    void updateBattery_WithValidRequest_ShouldReturnUpdatedBattery() throws Exception {
        BatteryResponse updatedResponse = BatteryResponse.builder()
                .id(1L)
                .name("Updated Battery")
                .postcode(2001)
                .wattCapacity(200L)
                .build();

        when(batteryService.updateBattery(eq(1L), any(BatteryRequest.class))).thenReturn(updatedResponse);

        mockMvc.perform(put("/api/v1/batteries/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(batteryRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Battery"))
                .andExpect(jsonPath("$.postcode").value(2001))
                .andExpect(jsonPath("$.wattCapacity").value(200));
    }

    @Test
    void deleteBattery_WithValidId_ShouldReturnNoContent() throws Exception {
        doNothing().when(batteryService).deleteBattery(1L);

        mockMvc.perform(delete("/api/v1/batteries/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void getAllBatteries_ShouldReturnPagedBatteries() throws Exception {
        BatteryResponse battery = BatteryResponse.builder()
                .id(1L)
                .name("Battery1")
                .postcode(2000)
                .wattCapacity(150L)
                .build();

        Page<BatteryResponse> page = new org.springframework.data.domain.PageImpl<>(List.of(battery));
        when(batteryService.getAllBatteries(any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/batteries/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("Battery1"));
    }

    @Test
    void getBatteryById_WhenBatteryNotFound_ShouldReturnNotFound() throws Exception {
        when(batteryService.getBatteryById(1L)).thenThrow(new BatteryDataException("Battery not found"));

        mockMvc.perform(get("/api/v1/batteries/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Battery not found"));
    }

    @Test
    void updateBattery_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        BatteryRequest invalidRequest = BatteryRequest.builder().build();

        mockMvc.perform(put("/api/v1/batteries/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateBattery_WhenServiceThrowsException_ShouldReturnBadRequest() throws Exception {
        when(batteryService.updateBattery(eq(1L), any(BatteryRequest.class)))
                .thenThrow(new BatteryDataException("Update failed"));

        mockMvc.perform(put("/api/v1/batteries/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(batteryRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Update failed"));
    }

    @Test
    void deleteBattery_WhenBatteryNotFound_ShouldReturnBadRequest() throws Exception {
        doThrow(new BatteryDataException("Battery not found")).when(batteryService).deleteBattery(1L);

        mockMvc.perform(delete("/api/v1/batteries/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Battery not found"));
    }

    @Test
    void getAllBatteries_WhenServiceThrowsException_ShouldReturnBadRequest() throws Exception {
        when(batteryService.getAllBatteries(any()))
                .thenThrow(new BatteryDataException("Fetch failed"));

        mockMvc.perform(get("/api/v1/batteries/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Fetch failed"));
    }





}