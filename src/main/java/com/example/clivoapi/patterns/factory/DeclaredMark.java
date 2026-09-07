package com.example.clivoapi.patterns.factory;

import com.example.clivoapi.common.extension.ComponentMark;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "component_mark")
public class DeclaredMark {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String component;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String label;

    @Column(nullable = false)
    private String rendering;

    @Column(name = "sort_order", nullable = false)
    private short sortOrder;

    protected DeclaredMark() {
    }

    ComponentMark mark() {
        return new ComponentMark(code, label, rendering);
    }
}
