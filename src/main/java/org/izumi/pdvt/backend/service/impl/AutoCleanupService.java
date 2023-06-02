package org.izumi.pdvt.backend.service.impl;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AutoCleanupService {
    private static final Logger log = LoggerFactory.getLogger(AutoCleanupService.class);
    private final Environment environment;
    private final ThreadPoolTaskScheduler scheduler;
    private final CheckClientsTask checkClientsTask;

    @EventListener
    public void init(ApplicationStartedEvent event) {
        final boolean enabled = environment.getProperty("auto-cleanup.enabled", Boolean.class, false);
        if (!enabled) {
            return;
        }

        final LocalDateTime midnight = atMidnight();
        scheduler.schedule(this::checkClientsWithScheduling, toInstant(midnight));

        log.info("Scheduled next clients cleanup execution at {}", midnight);
    }

    private void checkClientsWithScheduling() {
        checkClientsTask.run();
        final LocalDateTime nextExecutionTime = checkClientsTask.getNextExecutionTimeRecommendation();
        scheduler.schedule(this::checkClientsWithScheduling, toInstant(nextExecutionTime));

        log.info("Scheduled next clients cleanup execution at {}", nextExecutionTime);
    }

    private LocalDateTime atMidnight() {
        final LocalDateTime now = LocalDateTime.now();
        /*return now.plusDays(1)
            .withHour(0)
            .withSecond(0)
            .withMinute(0)
            .withNano(0)*/
        //TODO: uncomment

        return now.plusSeconds(5);
    }

    private Instant toInstant(LocalDateTime localDateTime) {
        return localDateTime.toInstant(defaultZoneOffset());
    }

    private ZoneOffset defaultZoneOffset() {
        return OffsetDateTime.now().getOffset();
    }
}
