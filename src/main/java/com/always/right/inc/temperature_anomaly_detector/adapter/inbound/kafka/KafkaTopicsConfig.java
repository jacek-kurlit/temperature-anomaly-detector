package com.always.right.inc.temperature_anomaly_detector.adapter.inbound.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicsConfig {

    @Bean
    NewTopic temperatureMeasurementsTopic() {
        return TopicBuilder.name("temperature-measurements")
                .partitions(2)
                .replicas(1)
                .build();
    }

    @Bean
    NewTopic temperatureAnomaliesTopic() {
        return TopicBuilder.name("temperature-anomalies")
                .partitions(2)
                .replicas(1)
                .build();
    }
}
