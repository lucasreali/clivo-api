package com.example.clivoapi.common;

import static org.hibernate.annotations.UuidGenerator.Style.VERSION_7;

import com.example.clivoapi.common.audit.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "sample_entity")
public class SampleEntity extends AuditableEntity {

    @Id
    @UuidGenerator(style = VERSION_7)
    private UUID id;

    @Column(nullable = false)
    private String label;

    protected SampleEntity() {
    }

    public SampleEntity(String label) {
        this.label = label;
    }

    public UUID id() {
        return id;
    }

    public void relabel(String label) {
        this.label = label;
    }
}
