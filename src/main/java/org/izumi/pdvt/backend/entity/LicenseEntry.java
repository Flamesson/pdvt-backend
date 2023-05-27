package org.izumi.pdvt.backend.entity;

import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;

@JmixEntity
@Table(name = "LICENSE_ENTRY")
@Entity
public class LicenseEntry {

    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @Column(name = "ARTIFACT")
    private String artifact;

    @Column(name = "PROBLEMATIC")
    private Boolean problematic;

    @Column(name = "LICENSE_NAME")
    private String licenseName;

    public String getLicenseName() {
        return licenseName;
    }

    public void setLicenseName(String licenseName) {
        this.licenseName = licenseName;
    }

    public Boolean getProblematic() {
        return problematic;
    }

    public void setProblematic(Boolean problematic) {
        this.problematic = problematic;
    }

    public String getArtifact() {
        return artifact;
    }

    public void setArtifact(String artifact) {
        this.artifact = artifact;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}