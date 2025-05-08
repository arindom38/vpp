package com.challenge.vpp.repo;

import com.challenge.vpp.model.Battery;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BatteryRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("test_db")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private BatteryRepository batteryRepository;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void findByPostcodeBetween_ShouldReturnBatteriesInRange() {
        // Arrange
        Battery battery1 = Battery.builder()
                .name("Battery1")
                .postcode(2000)
                .wattCapacity(100L)
                .build();
        Battery battery2 = Battery.builder()
                .name("Battery2")
                .postcode(2001)
                .wattCapacity(200L)
                .build();
        Battery battery3 = Battery.builder()
                .name("Battery3")
                .postcode(3000)
                .wattCapacity(300L)
                .build();

        batteryRepository.saveAll(Arrays.asList(battery1, battery2, battery3));

        // Act
        List<Battery> result = batteryRepository.findByPostcodeBetween(2000, 2001);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(b -> b.getName().equals("Battery1")));
        assertTrue(result.stream().anyMatch(b -> b.getName().equals("Battery2")));
    }

    @Test
    void findByPostcodeBetweenAndWattCapacityBetween_ShouldReturnFilteredBatteries() {
        // Arrange
        Battery battery1 = Battery.builder()
                .name("Battery1")
                .postcode(2000)
                .wattCapacity(100L)
                .build();
        Battery battery2 = Battery.builder()
                .name("Battery2")
                .postcode(2001)
                .wattCapacity(200L)
                .build();
        Battery battery3 = Battery.builder()
                .name("Battery3")
                .postcode(2002)
                .wattCapacity(300L)
                .build();

        batteryRepository.saveAll(Arrays.asList(battery1, battery2, battery3));

        // Act
        List<Battery> result = batteryRepository.findByPostcodeBetweenAndWattCapacityBetween(
                2000, 2002, 150L, 250L);

        // Assert
        assertEquals(1, result.size());
        assertEquals("Battery2", result.get(0).getName());
    }

    @Test
    void findByPostcodeBetweenAndWattCapacityGreaterThanEqual_ShouldReturnFilteredBatteries() {
        // Arrange
        Battery battery1 = Battery.builder()
                .name("Battery1")
                .postcode(2000)
                .wattCapacity(100L)
                .build();
        Battery battery2 = Battery.builder()
                .name("Battery2")
                .postcode(2001)
                .wattCapacity(200L)
                .build();

        batteryRepository.saveAll(Arrays.asList(battery1, battery2));

        // Act
        List<Battery> result = batteryRepository.findByPostcodeBetweenAndWattCapacityGreaterThanEqual(
                2000, 2001, 150L);

        // Assert
        assertEquals(1, result.size());
        assertEquals("Battery2", result.get(0).getName());
    }

    @Test
    void findByPostcodeBetweenAndWattCapacityLessThanEqual_ShouldReturnFilteredBatteries() {
        // Arrange
        Battery battery1 = Battery.builder()
                .name("Battery1")
                .postcode(2000)
                .wattCapacity(100L)
                .build();
        Battery battery2 = Battery.builder()
                .name("Battery2")
                .postcode(2001)
                .wattCapacity(200L)
                .build();

        batteryRepository.saveAll(Arrays.asList(battery1, battery2));

        // Act
        List<Battery> result = batteryRepository.findByPostcodeBetweenAndWattCapacityLessThanEqual(
                2000, 2001, 150L);

        // Assert
        assertEquals(1, result.size());
        assertEquals("Battery1", result.get(0).getName());
    }

    @Test
    void saveAll_ShouldPersistAllBatteries() {
        // Arrange
        Battery battery1 = Battery.builder()
                .name("Battery1")
                .postcode(2000)
                .wattCapacity(100L)
                .build();
        Battery battery2 = Battery.builder()
                .name("Battery2")
                .postcode(2001)
                .wattCapacity(200L)
                .build();

        // Act
        List<Battery> savedBatteries = batteryRepository.saveAll(Arrays.asList(battery1, battery2));

        // Assert
        assertEquals(2, savedBatteries.size());
        assertNotNull(savedBatteries.get(0).getId());
        assertNotNull(savedBatteries.get(1).getId());
    }

    @Test
    void findByPostcodeBetween_ShouldReturnEmptyList_WhenNoMatchingRecords() {
        // Act
        List<Battery> result = batteryRepository.findByPostcodeBetween(9999, 10000);

        // Assert
        assertTrue(result.isEmpty());
    }
}