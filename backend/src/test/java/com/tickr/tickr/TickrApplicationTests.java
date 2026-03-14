package com.tickr.tickr;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "SPRING_INTEGRATION_TESTS", matches = "true")
class TickrApplicationTests {

    @Test
    void contextLoads() {
    }

}
