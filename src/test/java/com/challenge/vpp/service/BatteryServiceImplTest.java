package com.challenge.vpp.service;

import com.challenge.vpp.dto.BatteryRequest;
import com.challenge.vpp.dto.BatteryStatisticsResponse;
import com.challenge.vpp.exception.BatteryDataException;
import com.challenge.vpp.exception.InvalidCapacityRangeException;
import com.challenge.vpp.exception.InvalidPostcodeRangeException;
import com.challenge.vpp.model.Battery;
import com.challenge.vpp.repo.BatteryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BatteryServiceImplTest {

    @Mock
    private BatteryRepository batteryRepository;

    @InjectMocks
    private BatteryServiceImpl batteryService;

    private List<Battery> mockBatteries;
    private List<BatteryRequest> mockBatteryRequests;

    @BeforeEach
    void setUp() {
        // Setup mock batteries
        mockBatteries = Arrays.asList(
                Battery.builder().name("Battery1").postcode(2000).wattCapacity(100L).build(),
                Battery.builder().name("Battery2").postcode(2001).wattCapacity(200L).build(),
                Battery.builder().name("Battery3").postcode(2002).wattCapacity(300L).build()
        );

        // Setup mock battery requests
        mockBatteryRequests = Arrays.asList(
                BatteryRequest.builder().name("Battery1").postcode(2000).capacity(100L).build(),
                BatteryRequest.builder().name("Battery2").postcode(2001).capacity(200L).build()
        );
    }

    @Test
    void saveAll_SuccessfulSave() {
        // Arrange
        when(batteryRepository.saveAll(anyList())).thenReturn(mockBatteries);

        // Act
        assertDoesNotThrow(() -> batteryService.saveAll(mockBatteryRequests));

        // Assert
        verify(batteryRepository, times(1)).saveAll(anyList());
    }

    @Test
    void saveAll_WithNullList_ThrowsBatteryDataException() {
        // Act & Assert
        assertThrows(BatteryDataException.class, () -> 
            batteryService.saveAll(null)
        );
        verify(batteryRepository, never()).saveAll(anyList());
    }

    @Test
    void saveAll_WithEmptyList_ThrowsBatteryDataException() {
        // Act & Assert
        assertThrows(BatteryDataException.class, () -> 
            batteryService.saveAll(Collections.emptyList())
        );
        verify(batteryRepository, never()).saveAll(anyList());
    }

    @Test
    void saveAll_WhenRepositoryThrowsException_ThrowsBatteryDataException() {
        // Arrange
        when(batteryRepository.saveAll(anyList())).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(BatteryDataException.class, () -> 
            batteryService.saveAll(mockBatteryRequests)
        );
    }

    @Test
    void getBatteriesInRange_SuccessfulRetrieval() {
        // Arrange
        when(batteryRepository.findByPostcodeBetween(2000, 2002))
                .thenReturn(mockBatteries);

        // Act
        BatteryStatisticsResponse response = batteryService.getBatteriesInRange(2000, 2002, null, null);

        // Assert
        assertNotNull(response);
        assertEquals(3, response.getBatteries().size());
        assertEquals(600L, response.getTotalWattCapacity());
        assertEquals(200.0, response.getAverageWattCapacity());
        assertTrue(response.getBatteries().containsAll(Arrays.asList("Battery1", "Battery2", "Battery3")));
    }

    @Test
    void getBatteriesInRange_WithCapacityRange_SuccessfulRetrieval() {
        // Arrange
        List<Battery> filteredBatteries = Collections.singletonList(mockBatteries.get(1)); // Only Battery2
        when(batteryRepository.findByPostcodeBetweenAndWattCapacityBetween(2000, 2002, 150L, 250L))
                .thenReturn(filteredBatteries);

        // Act
        BatteryStatisticsResponse response = batteryService.getBatteriesInRange(2000, 2002, 150L, 250L);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getBatteries().size());
        assertEquals(200L, response.getTotalWattCapacity());
        assertEquals(200.0, response.getAverageWattCapacity());
        assertEquals("Battery2", response.getBatteries().getFirst());
    }

    @Test
    void getBatteriesInRange_WithMinCapacityOnly_SuccessfulRetrieval() {
        // Arrange
        List<Battery> filteredBatteries = Arrays.asList(
                mockBatteries.get(1),
                mockBatteries.get(2)
        );
        when(batteryRepository.findByPostcodeBetweenAndWattCapacityGreaterThanEqual(2000, 2002, 150L))
                .thenReturn(filteredBatteries);

        // Act
        BatteryStatisticsResponse response = batteryService.getBatteriesInRange(2000, 2002, 150L, null);

        // Assert
        assertNotNull(response);
        assertEquals(2, response.getBatteries().size());
        assertEquals(500L, response.getTotalWattCapacity());
        assertEquals(250.0, response.getAverageWattCapacity());
    }

    @Test
    void getBatteriesInRange_WithMaxCapacityOnly_SuccessfulRetrieval() {
        // Arrange
        List<Battery> filteredBatteries = Arrays.asList(
                mockBatteries.get(0),
                mockBatteries.get(1)
        );
        when(batteryRepository.findByPostcodeBetweenAndWattCapacityLessThanEqual(2000, 2002, 250L))
                .thenReturn(filteredBatteries);

        // Act
        BatteryStatisticsResponse response = batteryService.getBatteriesInRange(2000, 2002, null, 250L);

        // Assert
        assertNotNull(response);
        assertEquals(2, response.getBatteries().size());
        assertEquals(300L, response.getTotalWattCapacity());
        assertEquals(150.0, response.getAverageWattCapacity());
    }

    @Test
    void getBatteriesInRange_WithInvalidPostcodeRange_ThrowsInvalidPostcodeRangeException() {
        // Act & Assert
        assertThrows(InvalidPostcodeRangeException.class, () ->
            batteryService.getBatteriesInRange(2002, 2000, null, null)
        );
        verify(batteryRepository, never()).findByPostcodeBetween(anyInt(), anyInt());
    }

    @Test
    void getBatteriesInRange_WithInvalidCapacityRange_ThrowsInvalidCapacityRangeException() {
        // Act & Assert
        assertThrows(InvalidCapacityRangeException.class, () ->
            batteryService.getBatteriesInRange(2000, 2002, 200L, 100L)
        );
        verify(batteryRepository, never()).findByPostcodeBetweenAndWattCapacityBetween(
            anyInt(), anyInt(), anyLong(), anyLong());
    }

    @Test
    void getBatteriesInRange_WhenRepositoryThrowsException_ThrowsBatteryDataException() {
        // Arrange
        when(batteryRepository.findByPostcodeBetween(anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(BatteryDataException.class, () ->
            batteryService.getBatteriesInRange(2000, 2002, null, null)
        );
    }

    @Test
    void getBatteriesInRange_WithNoResults_ReturnsEmptyResponse() {
        // Arrange
        when(batteryRepository.findByPostcodeBetween(anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        // Act
        BatteryStatisticsResponse response = batteryService.getBatteriesInRange(2000, 2002, null, null);

        // Assert
        assertNotNull(response);
        assertTrue(response.getBatteries().isEmpty());
        assertEquals(0L, response.getTotalWattCapacity());
        assertEquals(0.0, response.getAverageWattCapacity());
    }

    @Test
    void getBatteriesInRange_VerifyBatteriesAreSortedByName() {
        // Arrange
        List<Battery> unsortedBatteries = Arrays.asList(
                Battery.builder().name("Charlie").postcode(2000).wattCapacity(100L).build(),
                Battery.builder().name("Alpha").postcode(2001).wattCapacity(200L).build(),
                Battery.builder().name("Bravo").postcode(2002).wattCapacity(300L).build()
        );
        when(batteryRepository.findByPostcodeBetween(2000, 2002))
                .thenReturn(unsortedBatteries);

        // Act
        BatteryStatisticsResponse response = batteryService.getBatteriesInRange(2000, 2002, null, null);

        // Assert
        assertEquals(Arrays.asList("Alpha", "Bravo", "Charlie"), response.getBatteries());
    }
}