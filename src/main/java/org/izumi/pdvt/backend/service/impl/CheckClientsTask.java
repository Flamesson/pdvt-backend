package org.izumi.pdvt.backend.service.impl;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

import io.jmix.core.DataManager;
import io.jmix.core.SaveContext;
import io.jmix.core.security.SystemAuthenticator;
import lombok.RequiredArgsConstructor;
import org.izumi.pdvt.backend.entity.Client;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CheckClientsTask {
    private static final Integer DEFAULT_AUTO_CLEANUP_MAX_AGE_HOURS = 168;
    private final SystemAuthenticator authenticator;
    private final Environment environment;
    private final DataManager dataManager;

    public void run() {
        final Integer maxAge = environment.getProperty("auto-cleanup.max-age-hours", Integer.class, DEFAULT_AUTO_CLEANUP_MAX_AGE_HOURS);
        authenticator.runWithSystem(() -> {
            final Collection<Client> expired = getExpired(maxAge);
            if (expired.isEmpty()) {
                return;
            }

            dataManager.save(new SaveContext().removing(expired));
        });
    }

    public LocalDateTime getNextExecutionTimeRecommendation() {
        final CheckClientsTask task = this;
        return authenticator.withSystem(() -> {
            final Optional<Integer> period = Optional.ofNullable(
                    environment.getProperty("auto-cleanup.period-hours", Integer.class, null)
            );

            return period.map(hours -> LocalDateTime.now().plusHours(hours))
                    .orElseGet(task::pickupNextExpirationTime);
        });
    }

    private Collection<Client> getExpired(int masAgeHours) {
        final LocalDateTime date = LocalDateTime.now().minusHours(masAgeHours);
        return dataManager.load(Client.class)
                .query("SELECT c FROM Client c WHERE c.lastActivity < :date")
                .parameter("date", date)
                .list();
    }

    private LocalDateTime pickupNextExpirationTime() {
        final Optional<Client> optional = dataManager.load(Client.class)
                .query("SELECT c FROM Client c ORDER BY c.lastActivity ASC")
                .maxResults(1)
                .firstResult(0)
                .optional();

        final Integer maxAge = environment.getProperty("auto-cleanup.max-age-hours", Integer.class, DEFAULT_AUTO_CLEANUP_MAX_AGE_HOURS);
        return optional.map(client -> client.getLastActivity().plusHours(maxAge))
                .orElseGet(() -> LocalDateTime.now().plusHours(maxAge));
    }
}
