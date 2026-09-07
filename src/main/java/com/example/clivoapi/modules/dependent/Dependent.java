package com.example.clivoapi.modules.dependent;

import static org.hibernate.annotations.UuidGenerator.Style.VERSION_7;

import com.example.clivoapi.common.tenant.TenantScopedEntity;
import com.example.clivoapi.core.customer.Customer;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "dependent")
@EntityListeners(AuditingEntityListener.class)
public class Dependent extends TenantScopedEntity {

    @Id
    @UuidGenerator(style = VERSION_7)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false, updatable = false)
    private Customer customer;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "dependent_type", nullable = false)
    private DependentType type;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private Map<String, Object> attributes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DependentStatus status;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Dependent() {
    }

    public Dependent(Customer customer, DependentDetails details) {
        this.customer = customer;
        this.status = DependentStatus.ACTIVE;
        describeAs(details);
    }

    public UUID id() {
        return id;
    }

    public Optional<Integer> ageInYears() {
        return Optional.ofNullable(birthDate).map(Dependent::yearsSince);
    }

    public boolean isActive() {
        return status == DependentStatus.ACTIVE;
    }

    public boolean isCaredForBy(Customer other) {
        return customer.id().equals(other.id());
    }

    public void describeAs(DependentDetails details) {
        this.name = details.name();
        this.type = details.type();
        this.birthDate = details.birthDate();
        this.attributes = details.attributes().asMap();
    }

    public void deactivate() {
        status = DependentStatus.INACTIVE;
    }

    public DependentSnapshot snapshot() {
        return new DependentSnapshot(id, customer.id(), details(), status, ageInYears().orElse(null));
    }

    public DependentDetails details() {
        return new DependentDetails(name, type, birthDate, new DependentAttributes(attributes));
    }

    private static int yearsSince(LocalDate birthDate) {
        return Period.between(birthDate, LocalDate.now()).getYears();
    }
}
