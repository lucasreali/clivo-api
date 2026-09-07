package com.example.clivoapi.common.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Embeddable
public class ValueChange {

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "old_value", updatable = false)
    private Map<String, Object> previous;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "new_value", updatable = false)
    private Map<String, Object> current;

    protected ValueChange() {
    }

    public ValueChange(Map<String, Object> previous, Map<String, Object> current) {
        this.previous = previous;
        this.current = current;
    }

    public Map<String, Object> previous() {
        return previous;
    }

    public Map<String, Object> current() {
        return current;
    }
}
