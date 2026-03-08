package com.always.right.inc.temperature_anomaly_detector.adapter.outbound.mongo;

import com.always.right.inc.temperature_anomaly_detector.domain.TemperatureAnomaly;
import com.always.right.inc.temperature_anomaly_detector.domain.TemperatureAnomalyRepository;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TemperatureAnomalyMongoRepository extends TemperatureAnomalyRepository, MongoRepository<TemperatureAnomaly, UUID> {

    Optional<TemperatureAnomaly> findById(UUID id);

    TemperatureAnomaly save(TemperatureAnomaly temperatureAnomaly);

    boolean existsById(UUID id);
}
