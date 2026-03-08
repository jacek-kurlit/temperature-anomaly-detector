package com.always.right.inc.temperature_anomaly_detector;

import com.always.right.inc.temperature_anomaly_detector.adapter.inbound.kafka.TemperatureMeasurementEvent;
import org.bson.BsonDocument;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "api.key=" + E2EBaseTest.API_KEY,
        "anomaly-detection.window-size=" + E2EBaseTest.WINDOW_SIZE
})
public abstract class E2EBaseTest extends KafkaBaseTest {

    static final String API_KEY = "test-key";
    static final int WINDOW_SIZE = 10;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void cleanUp() {
        mongoTemplate.getCollectionNames()
                .forEach(collection -> mongoTemplate
                        .getCollection(collection)
                        .deleteMany(new BsonDocument()));
    }

    protected void sendMeasurement(String thermometerId, String roomId, double temperature, Instant timestamp) {
        kafkaTemplate.send(
                MessageBuilder.withPayload(new TemperatureMeasurementEvent(thermometerId, roomId, temperature, timestamp))
                        .setHeader(KafkaHeaders.TOPIC, "temperature-measurements")
                        .setHeader(KafkaHeaders.KEY, thermometerId)
                        .setHeader("__TypeId__", "temperature-measurement")
                        .build()
        );
    }
}
