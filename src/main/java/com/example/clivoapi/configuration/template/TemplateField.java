package com.example.clivoapi.configuration.template;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.extension.ModuleCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "template_field")
public class TemplateField {

    private static final String COMPONENT_TYPE = "COMPONENT";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_section_id", nullable = false)
    private TemplateSection section;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String label;

    @Column(name = "field_type", nullable = false)
    private String fieldType;

    private String component;

    @Column(nullable = false)
    private boolean required;

    @Column(name = "sort_order", nullable = false)
    private short sortOrder;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> options;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> validation;

    @Column(name = "requires_module")
    private String requiresModule;

    protected TemplateField() {
    }

    TemplateField(TemplateSection section, FieldContent content, short sortOrder) {
        requireComponentMatchesType(content);
        this.section = section;
        this.code = content.code();
        this.label = content.label();
        this.fieldType = content.fieldType();
        this.component = content.component();
        this.required = content.required();
        this.sortOrder = sortOrder;
        this.options = content.options();
        this.validation = content.validation();
        this.requiresModule = content.requiresModule();
    }

    public FieldContent content() {
        return new FieldContent(code, label, fieldType, component, required, options, validation, requiresModule);
    }

    public Optional<ModuleCode> requiredModule() {
        return Optional.ofNullable(requiresModule).map(ModuleCode::new);
    }

    TemplateField copyInto(TemplateSection target) {
        return new TemplateField(target, content(), sortOrder);
    }

    private void requireComponentMatchesType(FieldContent content) {
        if (COMPONENT_TYPE.equals(content.fieldType()) == (content.component() != null)) {
            return;
        }
        throw new BusinessException(
                "field %s must name a component when its type is %s, and none otherwise"
                        .formatted(content.code(), COMPONENT_TYPE));
    }
}
