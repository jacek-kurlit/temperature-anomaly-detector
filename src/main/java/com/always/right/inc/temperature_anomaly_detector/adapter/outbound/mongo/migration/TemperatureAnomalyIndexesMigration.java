package com.always.right.inc.temperature_anomaly_detector.adapter.outbound.mongo.migration;

import com.always.right.inc.temperature_anomaly_detector.domain.TemperatureAnomaly;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.CompoundIndexDefinition;
import org.springframework.data.mongodb.core.index.Index;

@ChangeUnit(id = "create-temperature-anomaly-indexes", order = "001", author = "Jacek Kurlit")
public class TemperatureAnomalyIndexesMigration {

    @Execution
    public void createIndexes(MongoTemplate mongoTemplate) {
        var indexOps = mongoTemplate.indexOps(TemperatureAnomaly.class);

        // supports queries by thermometer + time range sorting (used by /thermometerId/anomalies and aggregation)
        indexOps.createIndex(new CompoundIndexDefinition(
                new org.bson.Document("thermometerId", 1).append("createdAt", -1)
        ));

        // supports queries by room + time range sorting (used by /rooms/roomId/anomalies)
        indexOps.createIndex(new CompoundIndexDefinition(
                new org.bson.Document("roomId", 1).append("createdAt", -1)
        ));

        // supports the $match on createdAt in the above-threshold aggregation pipeline
        indexOps.createIndex(new Index("createdAt", Sort.Direction.DESC));
    }

    @RollbackExecution
    public void rollbackIndexes(MongoTemplate mongoTemplate) {
        var indexOps = mongoTemplate.indexOps(TemperatureAnomaly.class);
        indexOps.dropIndex("thermometerId_1_createdAt_-1");
        indexOps.dropIndex("roomId_1_createdAt_-1");
        indexOps.dropIndex("createdAt_-1");
    }
}
