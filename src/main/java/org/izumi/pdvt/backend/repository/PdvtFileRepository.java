package org.izumi.pdvt.backend.repository;

import java.util.Optional;
import java.util.function.Consumer;

import io.jmix.core.FetchPlanBuilder;
import org.izumi.pdvt.backend.entity.Client;
import org.izumi.pdvt.backend.entity.PdvtFile;

public interface PdvtFileRepository {
    Optional<PdvtFile> findByClientAndFileDtoName(Consumer<FetchPlanBuilder> configurer, Client client, String name);
}
