package com.springgears.micrometer.controller;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.config.MeterFilterReply;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/metrics")
public class MetricsController {

    private final MeterRegistry meterRegistry;
    private Counter devCounter;
    private Counter prodCounter;

    @Autowired
    public MetricsController(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @GetMapping("timer")
    public void timer() {
        Timer.Sample sample = Timer.start(meterRegistry);
        Timer timer = Timer.builder("gears.time")
                .register(meterRegistry);
        sample.stop(timer);
    }

    @GetMapping("counter")
    public void counter() {
        devCounter.increment();
        prodCounter.increment();
    }

    @GetMapping("gauge")
    public void gauge() {
        meterRegistry.gauge("number.of.retries", 5);

        List<String> names = Arrays.asList("Alex", "Michael", "Sarah");
        meterRegistry.gaugeCollectionSize("people.registered", Collections.emptyList(), names);
    }

    @GetMapping("summary")
    public void distributionSummary() {
        DistributionSummary summary = meterRegistry.summary("particle.length");
        IntStream.range(0, 42)
                .forEach(summary::record);
    }

    @PostConstruct
    public void init() {
        meterRegistry.config().meterFilter(new MeterFilter() {
            @Override
            public MeterFilterReply accept(Meter.Id id) {
                String environmentTag = id.getTag("environment");
                if (environmentTag != null && environmentTag.equals("dev")) {
                    return MeterFilterReply.DENY;
                }
                return MeterFilterReply.NEUTRAL;
            }
        });

        devCounter = Counter.builder("number.of.requests")
                .tag("environment", "dev")
                .register(meterRegistry);

        prodCounter = Counter.builder("number.of.requests")
                .tag("environment", "prod")
                .register(meterRegistry);

        devCounter.increment();
        prodCounter.increment();
    }
}
