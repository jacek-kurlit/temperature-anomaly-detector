package com.always.right.inc.temperature_anomaly_detector;

import org.springframework.boot.SpringApplication;

public class TestTemperatureAnomalyDetectorApplication {

	public static void main(String[] args) {
		SpringApplication.from(TemperatureAnomalyDetectorApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
