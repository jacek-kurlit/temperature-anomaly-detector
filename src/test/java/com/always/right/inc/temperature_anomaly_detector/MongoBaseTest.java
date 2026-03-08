package com.always.right.inc.temperature_anomaly_detector;

import org.bson.BsonDocument;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.testcontainers.mongodb.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

@DataMongoTest
@Import(MongoBaseTest.ContainerConfig.class)
public abstract class MongoBaseTest {

    @Autowired
    protected MongoTemplate mongoTemplate;

    @BeforeEach
    void cleanUp() {
        mongoTemplate.getCollectionNames()
                .forEach(collection -> mongoTemplate
                        .getCollection(collection)
                        .deleteMany(new BsonDocument()));
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class ContainerConfig {

        @Bean
        @ServiceConnection
        MongoDBContainer mongoDBContainer() {
            return new MongoDBContainer(DockerImageName.parse("mongo:latest"));
        }
    }
}
