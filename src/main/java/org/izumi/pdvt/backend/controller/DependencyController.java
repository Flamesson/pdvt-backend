package org.izumi.pdvt.backend.controller;

import java.util.Collection;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import io.jmix.core.security.SystemAuthenticator;
import lombok.RequiredArgsConstructor;
import org.izumi.pdvt.backend.dto.DependencyDto;
import org.izumi.pdvt.backend.entity.LicenseEntry;
import org.izumi.pdvt.backend.repository.LicenseEntryRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class DependencyController {
    private final SystemAuthenticator authenticator;
    private final LicenseEntryRepository licenseEntryRepository;

    @PostMapping("/licenses/check")
    public Collection<DependencyDto> checkLicenses(Collection<DependencyDto> dtos) {
        return authenticator.withSystem(() -> {
            final Iterable<LicenseEntry> entries = licenseEntryRepository.findAll();
            return dtos.stream()
                    .filter(dto -> isProblematic(entries, dto))
                    .collect(Collectors.toList());
        });
    }

    private boolean isProblematic(Iterable<LicenseEntry> entries, DependencyDto dto) {
        for (LicenseEntry entry : entries) {
            final Pattern pattern = Pattern.compile(entry.getArtifact().replaceAll("\\+", "?."));
            final boolean problematic = pattern.matcher(dto.getArtifact()).matches();
            if (problematic) {
                return true;
            }
        }

        return false;
    }
}
