package com.example.clivoapi.patterns.factory;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.extension.ModuleCode;
import com.example.clivoapi.common.extension.RecordValues;
import com.example.clivoapi.common.extension.SheetField;
import com.example.clivoapi.configuration.template.FieldContent;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class FieldDefinition {

    private final FieldContent content;

    public FieldDefinition(FieldContent content) {
        this.content = content;
    }

    public String code() {
        return content.code();
    }

    public String fieldType() {
        return content.fieldType();
    }

    public Optional<String> component() {
        return Optional.ofNullable(content.component());
    }

    public Optional<ModuleCode> requiredModule() {
        return Optional.ofNullable(content.requiresModule()).map(ModuleCode::new);
    }

    public List<String> options() {
        return Optional.ofNullable(content.options()).orElseGet(List::of);
    }

    public List<String> declaredOptions() {
        List<String> declared = options();
        if (!declared.isEmpty()) {
            return declared;
        }
        throw refusal("must declare the options it accepts");
    }

    public Optional<Object> valueIn(RecordValues values) {
        return values.valueOf(code());
    }

    public Optional<BigDecimal> rule(String name) {
        return validation()
                .map(rules -> rules.get(name))
                .filter(Number.class::isInstance)
                .map(Object::toString)
                .map(BigDecimal::new);
    }

    public void requirePresenceIn(RecordValues values) {
        if (isSatisfiedBy(values)) {
            return;
        }
        throw refusal("is required");
    }

    public SheetField renderedWith(RecordValues values) {
        return renderedAs(fieldType(), values, options());
    }

    public SheetField renderedAs(String rendering, RecordValues values, List<String> options) {
        return new SheetField(
                code(), content.label(), rendering, content.required(), options, valueIn(values).orElse(null));
    }

    public BusinessException refusal(String problem) {
        return new BusinessException("field %s (%s) %s".formatted(code(), content.label(), problem));
    }

    private boolean isSatisfiedBy(RecordValues values) {
        return !content.required() || !values.lacks(code());
    }

    private Optional<Map<String, Object>> validation() {
        return Optional.ofNullable(content.validation());
    }
}
