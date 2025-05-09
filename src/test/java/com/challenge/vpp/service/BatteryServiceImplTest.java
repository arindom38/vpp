package com.challenge.vpp.service;

import com.challenge.vpp.dto.BatteryRequest;
import com.challenge.vpp.dto.BatteryResponse;
import com.challenge.vpp.dto.BatteryStatisticsResponse;
import com.challenge.vpp.exception.BatteryDataException;
import com.challenge.vpp.exception.InvalidCapacityRangeException;
import com.challenge.vpp.exception.InvalidPostcodeRangeException;
import com.challenge.vpp.exception.ResourceNotFoundException;
import com.challenge.vpp.model.Battery;
import com.challenge.vpp.repo.BatteryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

    @Test
    void getBatteryById_WhenBatteryExists_ReturnsCorrectBattery() {
        // Arrange
        Long batteryId = 1L;
        Battery battery = Battery.builder()
                .name("Battery1")
                .postcode(2000)
                .wattCapacity(100L)
                .build();
        battery.setId(batteryId);
        when(batteryRepository.findById(batteryId)).thenReturn(Optional.of(battery));

        // Act
        BatteryResponse response = batteryService.getBatteryById(batteryId);

        // Assert
        assertNotNull(response);
        assertEquals(batteryId, response.getId());
        assertEquals("Battery1", response.getName());
        assertEquals(2000, response.getPostcode());
        assertEquals(100L, response.getWattCapacity());
    }

    @Test
    void getBatteryById_WhenBatteryNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        Long batteryId = 1L;
        when(batteryRepository.findById(batteryId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                batteryService.getBatteryById(batteryId)
        );
    }

    @Test
    void deleteBattery_WhenBatteryExists_DeletesSuccessfully() {
        // Arrange
        Long batteryId = 1L;
        when(batteryRepository.existsById(batteryId)).thenReturn(true);

        // Act
        assertDoesNotThrow(() -> batteryService.deleteBattery(batteryId));

        // Assert
        verify(batteryRepository).deleteById(batteryId);
    }

    @Test
    void deleteBattery_WhenBatteryNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        Long batteryId = 1L;
        when(batteryRepository.existsById(batteryId)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                batteryService.deleteBattery(batteryId)
        );
        verify(batteryRepository, never()).deleteById(anyLong());
    }

    @Test
    void updateBattery_WhenBatteryExists_UpdatesSuccessfully() {
        // Arrange
        Long batteryId = 1L;
        Battery existingBattery = Battery.builder()
                .name("OldName")
                .postcode(2000)
                .wattCapacity(100L)
                .build();
        existingBattery.setId(batteryId);
        BatteryRequest updateRequest = BatteryRequest.builder()
                .name("NewName")
                .postcode(2001)
                .capacity(200L)
                .build();
        Battery updatedBattery = Battery.builder()
                .name("NewName")
                .postcode(2001)
                .wattCapacity(200L)
                .build();
        updatedBattery.setId(batteryId);

        when(batteryRepository.findById(batteryId)).thenReturn(Optional.of(existingBattery));
        when(batteryRepository.save(any(Battery.class))).thenReturn(updatedBattery);

        // Act
        BatteryResponse response = batteryService.updateBattery(batteryId, updateRequest);

        // Assert
        assertNotNull(response);
        assertEquals(batteryId, response.getId());
        assertEquals("NewName", response.getName());
        assertEquals(2001, response.getPostcode());
        assertEquals(200L, response.getWattCapacity());
        verify(batteryRepository).save(any(Battery.class));
    }

    @Test
    void updateBattery_WhenBatteryNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        Long batteryId = 1L;
        BatteryRequest updateRequest = BatteryRequest.builder()
                .name("NewName")
                .postcode(2001)
                .capacity(200L)
                .build();
        when(batteryRepository.findById(batteryId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                batteryService.updateBattery(batteryId, updateRequest)
        );
        verify(batteryRepository, never()).save(any(Battery.class));
    }

    @Test
    void getAllBatteries_ReturnsPagedResults() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Battery> batteryPage = new PageImpl<>(mockBatteries, pageable, mockBatteries.size());
        when(batteryRepository.findAll(pageable)).thenReturn(batteryPage);

        // Act
        Page<BatteryResponse> response = batteryService.getAllBatteries(pageable);

        // Assert
        assertNotNull(response);
        assertEquals(3, response.getContent().size());
        assertEquals(mockBatteries.get(0).getName(), response.getContent().get(0).getName());
        assertEquals(mockBatteries.get(0).getPostcode(), response.getContent().get(0).getPostcode());
        assertEquals(mockBatteries.get(0).getWattCapacity(), response.getContent().get(0).getWattCapacity());
    }

    @Test
    void getAllBatteries_WhenEmpty_ReturnsEmptyPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Battery> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(batteryRepository.findAll(pageable)).thenReturn(emptyPage);

        // Act
        Page<BatteryResponse> response = batteryService.getAllBatteries(pageable);

        // Assert
        assertNotNull(response);
        assertTrue(response.getContent().isEmpty());
        assertEquals(0, response.getTotalElements());
    }


}