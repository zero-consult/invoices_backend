package org.zero_consult.invoices_backend.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class MockableClock {
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
