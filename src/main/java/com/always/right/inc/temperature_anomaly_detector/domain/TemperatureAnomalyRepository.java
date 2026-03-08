package com.always.right.inc.temperature_anomaly_detector.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TemperatureAnomalyRepository {

    Optional<TemperatureAnomaly> findById(UUID id);

    boolean existsById(UUID id);

    TemperatureAnomaly save(TemperatureAnomaly temperatureAnomaly);

    Page<TemperatureAnomaly> findByThermometerIdOrderByCreatedAtDesc(String thermometerId, Pageable pageable);

    Page<TemperatureAnomaly> findByRoomIdOrderByCreatedAtDesc(String roomId, Pageable pageable);

    List<ThermometerAnomalyCount> findThermometersWithAnomalyCountExceeding(long threshold, Instant fromDate);
}
