package com.always.right.inc.temperature_anomaly_detector;

import org.assertj.core.api.WithAssertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest
public abstract class RestBaseTest implements WithAssertions {

    @Autowired
    protected MockMvc mockMvc;
}
