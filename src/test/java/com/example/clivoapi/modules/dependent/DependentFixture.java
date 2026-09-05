package com.example.clivoapi.modules.dependent;

import com.example.clivoapi.common.DatabaseTest;
import com.example.clivoapi.common.extension.ModuleCode;
import com.example.clivoapi.common.tenant.Tenant;
import com.example.clivoapi.configuration.modules.ModuleActivationService;
import com.example.clivoapi.core.customer.ContactDetails;
import com.example.clivoapi.core.customer.CustomerDetails;
import com.example.clivoapi.core.customer.CustomerService;
import java.time.LocalDate;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;

abstract class DependentFixture extends DatabaseTest {

    protected static final ModuleCode DEPENDENT = new ModuleCode("dependent");

    @Autowired
    protected DependentService dependents;

    @Autowired
    private ModuleActivationService modules;

    @Autowired
    private CustomerService customers;

    protected Tenant openClinicWithDependents(String code) {
        Tenant clinic = openClinic(code);
        inTenant(clinic, () -> modules.activate(DEPENDENT));
        return clinic;
    }

    protected Tenant openClinic(String code) {
        Tenant clinic = createTenant(code);
        bindTenant(clinic);
        return clinic;
    }

    protected Long registerCustomer(String name) {
        return customers.register(new CustomerDetails(
                        name, null, LocalDate.of(1985, 3, 12), new ContactDetails("41999990000", null), null))
                .id();
    }

    protected DependentDetails aPuppyNamed(String name) {
        return new DependentDetails(
                name,
                DependentType.ANIMAL,
                LocalDate.now().minusYears(3).minusMonths(2),
                new DependentAttributes(Map.of("species", "dog", "breed", "beagle")));
    }
}
