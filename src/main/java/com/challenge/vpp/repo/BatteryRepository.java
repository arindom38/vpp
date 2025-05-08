package com.challenge.vpp.repo;

import com.challenge.vpp.model.Battery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BatteryRepository extends JpaRepository<Battery, Long> {
    List<Battery> findByPostcodeBetween(int from, int to);
    List<Battery> findByPostcodeBetweenAndWattCapacityBetween(int from, int to, long minCapacity, long maxCapacity);
    List<Battery> findByPostcodeBetweenAndWattCapacityGreaterThanEqual(int from, int to, long minCapacity);
    List<Battery> findByPostcodeBetweenAndWattCapacityLessThanEqual(int from, int to, long maxCapacity);
}
