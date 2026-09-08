package com.example.clivoapi.patterns.factory;

import com.example.clivoapi.common.extension.ComponentMark;
import com.example.clivoapi.common.extension.MarkTarget;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "applies_to", nullable = false)
    private MarkTarget appliesTo;

    protected DeclaredMark() {
    }

    ComponentMark mark() {
        return new ComponentMark(code, label, rendering, appliesTo);
    }
}
