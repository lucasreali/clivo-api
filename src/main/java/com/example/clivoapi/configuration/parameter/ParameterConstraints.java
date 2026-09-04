package com.example.clivoapi.configuration.parameter;

import com.example.clivoapi.common.extension.ParameterValue;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Embeddable
public class ParameterConstraints {

    private static final String NO_LIMITS = "of any size";

    private static final ObjectMapper JSON = new ObjectMapper();

    @Column(name = "min_value")
    private BigDecimal minimum;

    @Column(name = "max_value")
    private BigDecimal maximum;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "options")
    private List<String> options;

    protected ParameterConstraints() {
    }

    boolean acceptsWholeNumber(ParameterValue value) {
        return acceptsDecimal(value) && hasNoFraction(value);
    }

    boolean acceptsDecimal(ParameterValue value) {
        return decimalOf(value).filter(this::withinLimits).isPresent();
    }

    boolean acceptsFlag(ParameterValue value) {
        return List.of("true", "false").contains(value.asText().trim());
    }

    boolean acceptsOption(ParameterValue value) {
        return declaredOptions().contains(value.asText().trim());
    }

    boolean acceptsItems(ParameterValue value) {
        return declaredOptions().containsAll(itemsOf(value));
    }

    boolean acceptsReference(ParameterValue value) {
        return !value.isBlank();
    }

    String rangeDescription() {
        return Optional.ofNullable(minimum)
                .map(lowest -> "between %s and %s".formatted(plain(lowest), plain(maximum)))
                .orElse(NO_LIMITS);
    }

    String optionsDescription() {
        return String.join(", ", declaredOptions());
    }

    private List<String> itemsOf(ParameterValue value) {
        try {
            return JSON.readValue(value.asText(), new TypeReference<List<String>>() {});
        } catch (JacksonException notAJsonArray) {
            return List.of(value.asText().trim());
        }
    }

    private List<String> declaredOptions() {
        return Optional.ofNullable(options).orElseGet(List::of);
    }

    private boolean withinLimits(BigDecimal number) {
        return notBelowMinimum(number) && notAboveMaximum(number);
    }

    private boolean notBelowMinimum(BigDecimal number) {
        return minimum == null || number.compareTo(minimum) >= 0;
    }

    private boolean notAboveMaximum(BigDecimal number) {
        return maximum == null || number.compareTo(maximum) <= 0;
    }

    private boolean hasNoFraction(ParameterValue value) {
        return decimalOf(value).filter(number -> number.stripTrailingZeros().scale() <= 0).isPresent();
    }

    private Optional<BigDecimal> decimalOf(ParameterValue value) {
        try {
            return Optional.of(value.asDecimal());
        } catch (NumberFormatException notANumber) {
            return Optional.empty();
        }
    }

    private String plain(BigDecimal number) {
        return number.stripTrailingZeros().toPlainString();
    }
}
