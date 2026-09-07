package com.example.clivoapi.modules.dependent;

import com.example.clivoapi.common.exception.ResourceNotFoundException;
import com.example.clivoapi.core.customer.CustomerService;
import com.example.clivoapi.modules.dependent.internal.DependentRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DependentService {

    private final DependentRepository dependents;
    private final CustomerService customers;

    DependentService(DependentRepository dependents, CustomerService customers) {
        this.dependents = dependents;
        this.customers = customers;
    }

    public DependentSnapshot register(UUID customerId, DependentDetails details) {
        Dependent dependent = new Dependent(customers.reference(customerId), details);
        return dependents.save(dependent).snapshot();
    }

    public DependentSnapshot describe(UUID id, DependentDetails details) {
        Dependent dependent = dependentOf(id);
        dependent.describeAs(details);
        return dependents.save(dependent).snapshot();
    }

    public DependentSnapshot deactivate(UUID id) {
        Dependent dependent = dependentOf(id);
        dependent.deactivate();
        return dependents.save(dependent).snapshot();
    }

    @Transactional(readOnly = true)
    public List<DependentSnapshot> caredForBy(UUID customerId) {
        return dependents.findByCustomerIdOrderByNameAsc(customerId).stream()
                .map(Dependent::snapshot)
                .toList();
    }

    @Transactional(readOnly = true)
    public DependentSnapshot findOne(UUID id) {
        return dependentOf(id).snapshot();
    }

    private Dependent dependentOf(UUID id) {
        return dependents.findById(id).orElseThrow(() -> new ResourceNotFoundException("Dependent", id));
    }
}
