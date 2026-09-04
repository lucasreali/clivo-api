package com.example.clivoapi.core.customer;

import com.example.clivoapi.common.tenant.TenantScopedEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "customer")
@EntityListeners(AuditingEntityListener.class)
public class Customer extends TenantScopedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Embedded
    private NationalId nationalId;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Embedded
    private ContactDetails contact;

    @Embedded
    private Address address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerStatus status;

    @Column(name = "deactivation_reason")
    private String deactivationReason;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @LastModifiedBy
    @Column(name = "updated_by")
    private Long updatedBy;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ConsentRecord> consents = new ArrayList<>();

    protected Customer() {
    }

    public Customer(CustomerDetails details) {
        this.status = CustomerStatus.ACTIVE;
        describeAs(details);
    }

    public Long id() {
        return id;
    }

    public boolean isActive() {
        return status == CustomerStatus.ACTIVE;
    }

    public boolean hasValidConsent() {
        return consentHistory().grants(ConsentPurpose.dataProcessing());
    }

    public ConsentHistory consentHistory() {
        return new ConsentHistory(consents);
    }

    public void describeAs(CustomerDetails details) {
        this.name = details.name();
        this.nationalId = details.nationalId();
        this.birthDate = details.birthDate();
        this.contact = details.contact();
        this.address = details.address();
    }

    public void deactivate(DeactivationReason reason) {
        status = CustomerStatus.INACTIVE;
        deactivationReason = reason.asText();
    }

    public void record(ConsentStatement statement) {
        consents.add(new ConsentRecord(this, statement));
    }

    public CustomerSnapshot snapshot() {
        return new CustomerSnapshot(id, details(), status, deactivationReason, hasValidConsent());
    }

    public CustomerDetails details() {
        return new CustomerDetails(name, nationalId, birthDate, contact, address);
    }
}
