package com.example.clivoapi.patterns.factory;

import com.example.clivoapi.common.extension.ComponentDescriptor;
import com.example.clivoapi.common.extension.ComponentMark;
import com.example.clivoapi.common.extension.ComponentRegion;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "component_chart")
public class DeclaredChart {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String component;

    @Column(nullable = false)
    private String variant;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private List<String> groupings;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private List<ComponentRegion> regions;

    protected DeclaredChart() {
    }

    ComponentDescriptor describedWith(List<ComponentMark> vocabulary) {
        return new ComponentDescriptor(component, groupings, regions, vocabulary);
    }
}
