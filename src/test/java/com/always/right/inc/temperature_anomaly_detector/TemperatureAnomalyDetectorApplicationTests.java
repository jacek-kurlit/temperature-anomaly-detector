package com.always.right.inc.temperature_anomaly_detector;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class TemperatureAnomalyDetectorApplicationTests {

	@Test
	void contextLoads() {
	}

}
