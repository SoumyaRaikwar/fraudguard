package com.fraudguard.config;

import com.fraudguard.external.MLServiceClient;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@TestConfiguration
@Profile("test")
public class TestConfig {

    @Bean
    @Primary
    public MLServiceClient mockMLServiceClient() {
        return Mockito.mock(MLServiceClient.class);
    }
}
