package org.izumi.pdvt.backend.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import io.jmix.core.metamodel.annotation.JmixEntity;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JmixEntity
@Table(name = "LICENSE_ENTRY")
@Entity
public class LicenseEntry extends StandardEntity {

    @Column(name = "ARTIFACT")
    private String artifact;

    @Column(name = "PROBLEMATIC")
    private Boolean problematic;

    @Column(name = "LICENSE_NAME")
    private String licenseName;
}