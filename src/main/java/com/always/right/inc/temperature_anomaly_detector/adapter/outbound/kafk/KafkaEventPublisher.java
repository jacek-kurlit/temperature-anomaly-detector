package com.always.right.inc.temperature_anomaly_detector.adapter.outbound.kafk;

import com.always.right.inc.temperature_anomaly_detector.service.EventPublisher;
import com.always.right.inc.temperature_anomaly_detector.service.TemperatureAnomalyDetectedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaEventPublisher implements EventPublisher {

    private static final String TOPIC = "temperature-anomalies";
    private static final String TYPE_ID = "temperature-anomaly-detected";

    private final KafkaTemplate<String, TemperatureAnomalyDetectedEvent> kafkaTemplate;

    @Override
    public void publish(TemperatureAnomalyDetectedEvent event) {
        kafkaTemplate.send(
                MessageBuilder.withPayload(event)
                        .setHeader(KafkaHeaders.TOPIC, TOPIC)
//                        .setHeader(KafkaHeaders.KEY, event.thermometerId())
                        .setHeader("__TypeId__", TYPE_ID)
                        .build()
        );
    }
}
