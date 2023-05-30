package org.izumi.pdvt.backend.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import io.jmix.core.security.SystemAuthenticator;
import lombok.RequiredArgsConstructor;
import org.izumi.pdvt.backend.dto.DependencyDto;
import org.izumi.pdvt.backend.entity.LicenseEntry;
import org.izumi.pdvt.backend.repository.LicenseEntryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequiredArgsConstructor
@RestController
public class DependencyController extends AbstractController {
    private final SystemAuthenticator authenticator;
    private final LicenseEntryRepository licenseEntryRepository;

    @PostMapping("/licenses/check")
    public ResponseEntity<Collection<DependencyDto>> checkLicenses(@RequestBody Collection<DependencyDto> dtos) {
        return authenticator.withSystem(() -> {
            final Iterable<LicenseEntry> entries = licenseEntryRepository.findAllByProblematic(true);
            return ok(findProblematics(entries, dtos));
        });
    }

    private Collection<DependencyDto> findProblematics(Iterable<LicenseEntry> entries, Collection<DependencyDto> dtos) {
        final Collection<DependencyDto> problematics = new ArrayList<>();
        for (LicenseEntry entry : entries) {
            problematics.addAll(findProblematics(entry, dtos));
        }

        return problematics;
    }

    private Collection<DependencyDto> findProblematics(LicenseEntry entry, Collection<DependencyDto> dtos) {
        final String artifact = entry.getArtifact();

        final String[] parts = artifact.split(":");
        final StringBuilder regex = new StringBuilder();
        for (int i = 0; i < parts.length - 1; i++) {
            final String part = parts[i];
            if (part.trim().equals("+")) {
                regex.append(".*").append(Pattern.quote(":"));
            } else {
                regex.append(Pattern.quote(part + ":"));
            }
        }
        final String part = parts[parts.length - 1];
        if (part.trim().equals("+")) {
            regex.append(".*");
        } else {
            regex.append(Pattern.quote(part ));
        }

        final Pattern pattern = Pattern.compile(regex.toString());
        return dtos.stream()
                .map(dto -> {
                    final boolean covered = pattern.matcher(dto.getArtifact()).matches();
                    if (!covered) {
                        return null;
                    }

                    final DependencyDto problematic = new DependencyDto();
                    problematic.setArtifact(dto.getArtifact());
                    problematic.setLicense(entry.getLicenseName().replaceAll(";", ", "));
                    return problematic;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
