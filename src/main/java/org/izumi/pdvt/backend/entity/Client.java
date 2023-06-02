package org.izumi.pdvt.backend.entity;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import io.jmix.core.FileStorage;
import io.jmix.core.FileStorageLocator;
import io.jmix.core.metamodel.annotation.JmixEntity;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JmixEntity
@Table(name = "CLIENT")
@Entity
public class Client extends StandardEntity {

    @OneToMany(mappedBy = "client", cascade = CascadeType.REMOVE)
    private Collection<PdvtFile> files;

    @Column(name = "CODE")
    private String code;

    @Column(name = "PASSWORD")
    private String password;

    @Column(name = "STORAGE_NAME")
    private String storageName;

    @Column(name = "LAST_ACTIVITY")
    private LocalDateTime lastActivity;

    public LocalDateTime getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(LocalDateTime lastActivity) {
        this.lastActivity = lastActivity;
    }

    public FileStorage getStorage(FileStorageLocator locator) {
        if (Objects.nonNull(storageName)) {
            return locator.getByName(storageName);
        } else {
            return locator.getDefault();
        }
    }
}