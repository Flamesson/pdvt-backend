package org.izumi.pdvt.backend.repository;

import java.util.UUID;

import io.jmix.core.repository.JmixDataRepository;
import org.izumi.pdvt.backend.entity.LicenseEntry;

public interface LicenseEntryRepository extends JmixDataRepository<LicenseEntry, UUID> {
}
