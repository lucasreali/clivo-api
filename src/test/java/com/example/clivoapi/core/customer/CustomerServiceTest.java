package com.example.clivoapi.core.customer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.example.clivoapi.common.DatabaseTest;
import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.tenant.Tenant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CustomerServiceTest extends DatabaseTest {

    private static final String DOCUMENT = "12345678901";

    @Autowired
    private CustomerService customers;

    @Test
    void registeringKeepsTheDetailsAndStartsActive() {
        bindTenant(createTenant("TEST-CUSTOMER"));

        CustomerSnapshot registered = customers.register(detailsOf("Ana Souza", DOCUMENT));

        assertThat(registered.status()).isEqualTo(CustomerStatus.ACTIVE);
        assertThat(registered.details().document()).contains(new NationalId(DOCUMENT));
        assertThat(registered.details().contact().phone()).isEqualTo("41999990000");
        assertThat(registered.consented()).isFalse();
    }

    @Test
    void aDocumentIsUniqueWithinAClinicAndFreeAcrossClinics() {
        Tenant north = createTenant("TEST-NORTH");
        Tenant south = createTenant("TEST-SOUTH");
        inTenant(north, () -> customers.register(detailsOf("Ana Souza", DOCUMENT)));

        inTenant(south, () -> customers.register(detailsOf("Ana Souza", DOCUMENT)));

        bindTenant(north);
        assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> customers.register(detailsOf("Bruno Lima", DOCUMENT)))
                .withMessageContaining("already belongs to another customer");
    }

    @Test
    void deactivationDemandsAReasonAndKeepsTheCustomer() {
        bindTenant(createTenant("TEST-CUSTOMER"));
        Long id = customers.register(detailsOf("Ana Souza", DOCUMENT)).id();

        assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> customers.deactivate(id, new DeactivationReason(" ")))
                .withMessageContaining("a reason is required");

        CustomerSnapshot deactivated = customers.deactivate(id, new DeactivationReason("moved to another city"));

        assertThat(deactivated.status()).isEqualTo(CustomerStatus.INACTIVE);
        assertThat(deactivated.reasonForDeactivation()).contains("moved to another city");
        assertThat(customers.findOne(id).id()).isEqualTo(id);
    }

    @Test
    void theLatestConsentForAPurposeAnswersForTheCustomer() {
        bindTenant(createTenant("TEST-CUSTOMER"));
        Long id = customers.register(detailsOf("Ana Souza", DOCUMENT)).id();

        assertThat(customers.record(id, statementOf(true)).consented()).isTrue();
        assertThat(customers.record(id, statementOf(false)).consented()).isFalse();
        assertThat(customers.findOne(id).consented()).isFalse();
    }

    @Test
    void searchMatchesPartOfTheNameWithinTheClinic() {
        Tenant north = createTenant("TEST-NORTH");
        Tenant south = createTenant("TEST-SOUTH");
        inTenant(north, () -> customers.register(detailsOf("Ana Souza", DOCUMENT)));
        inTenant(south, () -> customers.register(detailsOf("Ana Prado", null)));

        List<CustomerSnapshot> found = valueInTenant(north, () -> customers.search("ana"));

        assertThat(found).singleElement().extracting(snapshot -> snapshot.details().name()).isEqualTo("Ana Souza");
    }

    private ConsentStatement statementOf(boolean granted) {
        return new ConsentStatement(ConsentPurpose.dataProcessing(), granted, "RECEPTION_DESK");
    }

    private CustomerDetails detailsOf(String name, String document) {
        return new CustomerDetails(
                name,
                document == null ? null : new NationalId(document),
                LocalDate.of(1990, 5, 12),
                new ContactDetails("41999990000", "ana@clivo.test"),
                new Address("80000000", "Rua das Flores, 100"));
    }
}
