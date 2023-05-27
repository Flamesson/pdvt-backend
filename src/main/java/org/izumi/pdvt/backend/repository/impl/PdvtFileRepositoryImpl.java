package org.izumi.pdvt.backend.repository.impl;

import java.util.Optional;
import java.util.function.Consumer;

import io.jmix.core.DataManager;
import io.jmix.core.FetchPlanBuilder;
import lombok.RequiredArgsConstructor;
import org.izumi.pdvt.backend.entity.Client;
import org.izumi.pdvt.backend.entity.PdvtFile;
import org.izumi.pdvt.backend.repository.PdvtFileRepository;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class PdvtFileRepositoryImpl implements PdvtFileRepository {
    private final DataManager dataManager;

    @Override
    public Optional<PdvtFile> findByClientAndFileDtoName(Consumer<FetchPlanBuilder> configurer,
                                                         Client client,
                                                         String name) {
        return dataManager.load(PdvtFile.class)
                .query("SELECT pf FROM FileDto f " +
                        "INNER JOIN PdvtFile pf ON pf.file = f " +
                        "WHERE pf.client = :client AND f.name = :filename")
                .parameter("client", client)
                .parameter("filename", name)
                .fetchPlan(configurer)
                .optional();
    }
}
