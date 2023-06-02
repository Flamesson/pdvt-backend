package org.izumi.pdvt.backend.entity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.jmix.core.DeletePolicy;
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.metamodel.annotation.JmixEntity;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JmixEntity
@Table(name = "PDVT_FILE")
@Entity
public class PdvtFile extends StandardEntity {

    @JsonIgnore
    @JoinColumn(name = "CLIENT_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private Client client;

    @OnDeleteInverse(DeletePolicy.DENY)
    @JoinColumn(name = "FILE_ID")
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private StoredFile file;
}