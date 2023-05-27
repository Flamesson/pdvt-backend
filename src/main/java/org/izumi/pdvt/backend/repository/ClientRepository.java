package org.izumi.pdvt.backend.repository;

import java.util.Optional;
import java.util.UUID;

import io.jmix.core.repository.JmixDataRepository;
import org.izumi.pdvt.backend.entity.Client;

public interface ClientRepository extends JmixDataRepository<Client, UUID> {
    Optional<Client> findByCode(String code);
}
