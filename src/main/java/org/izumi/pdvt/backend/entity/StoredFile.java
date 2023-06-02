package org.izumi.pdvt.backend.entity;

import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;

@JmixEntity
@Table(name = "STORED_FILE")
@Entity
public class StoredFile {

    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "file")
    private PdvtFile pdvtFile;

    @InstanceName
    @Column(name = "NAME")
    private String name;

    @Column(name = "PATH")
    private String path;

    public PdvtFile getPdvtFile() {
        return pdvtFile;
    }

    public void setPdvtFile(PdvtFile pdvtFile) {
        this.pdvtFile = pdvtFile;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}