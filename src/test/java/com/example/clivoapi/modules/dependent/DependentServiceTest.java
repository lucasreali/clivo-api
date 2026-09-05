package com.example.clivoapi.modules.dependent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.tenant.Tenant;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DependentServiceTest extends DependentFixture {

    @Test
    void aDependentIsRegisteredUnderTheCustomerWhoAnswersForIt() {
        Tenant clinic = openClinic("TEST-DEP-REGISTER");

        DependentSnapshot puppy = inClinic(clinic, "Nina");

        assertThat(puppy.details().type()).isEqualTo(DependentType.ANIMAL);
        assertThat(puppy.details().type().custodianTitle()).isEqualTo("tutor");
        assertThat(puppy.status()).isEqualTo(DependentStatus.ACTIVE);
    }

    @Test
    void theAgeIsCountedFromTheBirthDate() {
        Tenant clinic = openClinic("TEST-DEP-AGE");

        DependentSnapshot puppy = inClinic(clinic, "Rex");

        assertThat(puppy.ageInYears()).contains(3);
    }

    @Test
    void aDependentWithoutBirthDateHasNoAge() {
        Tenant clinic = openClinic("TEST-DEP-NO-BIRTH");

        DependentSnapshot assisted = valueInTenant(clinic, () -> dependents.register(
                registerCustomer("Marta Alves"),
                new DependentDetails("Sr. Otávio", DependentType.ASSISTED, null, null)));

        assertThat(assisted.ageInYears()).isEmpty();
    }

    @Test
    void theAttributesOfEachTypeAreKeptWithoutClosingTheStructure() {
        Tenant clinic = openClinic("TEST-DEP-ATTRIBUTES");

        DependentSnapshot minor = valueInTenant(clinic, () -> dependents.register(
                registerCustomer("Paulo Dias"),
                new DependentDetails(
                        "Bento Dias",
                        DependentType.MINOR,
                        LocalDate.now().minusYears(8),
                        new DependentAttributes(Map.of("school", "Colégio Novo", "grade", "3")))));

        assertThat(minor.details().attributes().asMap()).containsEntry("school", "Colégio Novo");
        assertThat(minor.details().type().custodianTitle()).isEqualTo("guardian");
    }

    @Test
    void aDependentWithoutNameIsRefused() {
        assertThatThrownBy(() -> new DependentDetails(" ", DependentType.ANIMAL, null, null))
                .isInstanceOf(BusinessException.class)
                .hasMessage("a dependent needs a name");
    }

    @Test
    void anUnknownTypeIsRefused() {
        assertThatThrownBy(() -> DependentType.of("ROBOT"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("a dependent is one of [ANIMAL, MINOR, ASSISTED]");
    }

    @Test
    void aDeactivatedDependentStaysUnderTheCustomer() {
        Tenant clinic = openClinic("TEST-DEP-DEACTIVATE");
        DependentSnapshot puppy = inClinic(clinic, "Bidu");

        DependentSnapshot deactivated = valueInTenant(clinic, () -> dependents.deactivate(puppy.id()));

        assertThat(deactivated.status()).isEqualTo(DependentStatus.INACTIVE);
        assertThat(valueInTenant(clinic, () -> dependents.caredForBy(puppy.customerId()))).hasSize(1);
    }

    @Test
    void oneClinicNeverSeesTheDependentsOfAnother() {
        Tenant firstClinic = openClinic("TEST-DEP-TENANT-A");
        DependentSnapshot puppy = inClinic(firstClinic, "Mel");
        Tenant otherClinic = openClinic("TEST-DEP-TENANT-B");

        assertThat(valueInTenant(otherClinic, () -> dependents.caredForBy(puppy.customerId())))
                .isEmpty();
    }

    private DependentSnapshot inClinic(Tenant clinic, String name) {
        return valueInTenant(clinic, () -> dependents.register(registerCustomer("Ana Prado"), aPuppyNamed(name)));
    }
}
