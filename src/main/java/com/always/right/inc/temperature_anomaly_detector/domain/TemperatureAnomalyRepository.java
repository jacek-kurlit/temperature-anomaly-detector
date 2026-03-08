package com.always.right.inc.temperature_anomaly_detector.domain;

import java.util.Optional;
import java.util.UUID;

public interface TemperatureAnomalyRepository {

    Optional<TemperatureAnomaly> findById(UUID id);

    boolean existsById(UUID id);

    TemperatureAnomaly save(TemperatureAnomaly temperatureAnomaly);
}
