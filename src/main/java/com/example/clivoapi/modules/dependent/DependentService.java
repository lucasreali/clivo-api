package com.example.clivoapi.modules.dependent;

import com.example.clivoapi.common.exception.ResourceNotFoundException;
import com.example.clivoapi.core.customer.CustomerService;
import com.example.clivoapi.modules.dependent.internal.DependentRepository;
import java.util.List;
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

    public DependentSnapshot register(Long customerId, DependentDetails details) {
        Dependent dependent = new Dependent(customers.reference(customerId), details);
        return dependents.save(dependent).snapshot();
    }

    public DependentSnapshot describe(Long id, DependentDetails details) {
        Dependent dependent = dependentOf(id);
        dependent.describeAs(details);
        return dependents.save(dependent).snapshot();
    }

    public DependentSnapshot deactivate(Long id) {
        Dependent dependent = dependentOf(id);
        dependent.deactivate();
        return dependents.save(dependent).snapshot();
    }

    @Transactional(readOnly = true)
    public List<DependentSnapshot> caredForBy(Long customerId) {
        return dependents.findByCustomerIdOrderByNameAsc(customerId).stream()
                .map(Dependent::snapshot)
                .toList();
    }

    @Transactional(readOnly = true)
    public DependentSnapshot findOne(Long id) {
        return dependentOf(id).snapshot();
    }

    private Dependent dependentOf(Long id) {
        return dependents.findById(id).orElseThrow(() -> new ResourceNotFoundException("Dependent", id));
    }
}
