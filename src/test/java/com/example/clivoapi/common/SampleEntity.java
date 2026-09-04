package com.example.clivoapi.common;

import com.example.clivoapi.common.audit.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "sample_entity")
public class SampleEntity extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String label;

    protected SampleEntity() {
    }

    public SampleEntity(String label) {
        this.label = label;
    }

    public Long id() {
        return id;
    }

    public void relabel(String label) {
        this.label = label;
    }
}
