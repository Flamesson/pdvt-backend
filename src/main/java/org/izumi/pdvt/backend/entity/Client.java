package org.izumi.pdvt.backend.entity;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import io.jmix.core.FileStorage;
import io.jmix.core.FileStorageLocator;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;

@JmixEntity
@Table(name = "CLIENT")
@Entity
public class Client {

    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @OneToMany(mappedBy = "client")
    private Collection<PdvtFile> files;

    @Column(name = "CODE")
    private String code;

    @Column(name = "PASSWORD")
    private String password;

    @Column(name = "STORAGE_NAME")
    private String storageName;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public FileStorage getStorage(FileStorageLocator locator) {
        if (Objects.nonNull(storageName)) {
            return locator.getByName(storageName);
        } else {
            return locator.getDefault();
        }
    }

    public String getStorageName() {
        return storageName;
    }

    public void setStorageName(String storageName) {
        this.storageName = storageName;
    }

    public Collection<PdvtFile> getFiles() {
        return files;
    }

    public void setFiles(Collection<PdvtFile> files) {
        this.files = files;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}