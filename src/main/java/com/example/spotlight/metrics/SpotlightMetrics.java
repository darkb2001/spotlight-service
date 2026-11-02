package com.example.spotlight.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class SpotlightMetrics {

    private final Counter appliedCounter;

    public SpotlightMetrics(MeterRegistry registry) {
        this.appliedCounter = Counter.builder("spotlight.applied")
                .description("Number of spotlights applied")
                .tag("service", "spotlight-service")
                .register(registry);
    }

    public void recordApplied() {
        appliedCounter.increment();
    }
}
