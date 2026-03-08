package com.always.right.inc.temperature_anomaly_detector.adapter.outbound.mongo;

import com.always.right.inc.temperature_anomaly_detector.domain.TemperatureAnomaly;
import com.always.right.inc.temperature_anomaly_detector.domain.TemperatureAnomalyRepository;
import com.always.right.inc.temperature_anomaly_detector.domain.ThermometerAnomalyCount;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TemperatureAnomalyMongoRepository extends TemperatureAnomalyRepository, MongoRepository<TemperatureAnomaly, UUID> {

    Optional<TemperatureAnomaly> findById(UUID id);

    TemperatureAnomaly save(TemperatureAnomaly temperatureAnomaly);

    boolean existsById(UUID id);

    @Aggregation(pipeline = {
            "{ '$match': { 'createdAt': { '$gte': ?1 } } }",
            "{ '$group': { '_id': '$thermometerId', 'count': { '$sum': 1 } } }",
            "{ '$match': { 'count': { '$gt': ?0 } } }"
    })
    List<ThermometerAnomalyCount> findThermometersWithAnomalyCountExceeding(long threshold, Instant fromDate);
}
