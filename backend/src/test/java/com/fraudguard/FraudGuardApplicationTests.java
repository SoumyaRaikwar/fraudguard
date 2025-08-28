package com.fraudguard;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class FraudGuardApplicationTests {

    @Test
    void contextLoads() {
        // Application context should load successfully
    }

    @Test
    void mainMethodTest() {
        // Test main method runs without errors
        String[] args = {};
        FraudGuardApplication.main(args);
    }
}
