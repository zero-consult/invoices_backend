package org.zero_consult.payslip_backend.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.time.Clock;

@Component
public class MockableClock {
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
