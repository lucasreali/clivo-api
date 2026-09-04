package com.example.clivoapi.core.customer;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.exception.ResourceNotFoundException;
import com.example.clivoapi.core.customer.internal.CustomerRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CustomerService {

    private final CustomerRepository customers;

    CustomerService(CustomerRepository customers) {
        this.customers = customers;
    }

    public CustomerSnapshot register(CustomerDetails details) {
        requireDocumentFree(details, null);
        return customers.save(new Customer(details)).snapshot();
    }

    public CustomerSnapshot describe(Long id, CustomerDetails details) {
        Customer customer = customerOf(id);
        requireDocumentFree(details, customer.id());
        customer.describeAs(details);
        return customers.save(customer).snapshot();
    }

    public CustomerSnapshot deactivate(Long id, DeactivationReason reason) {
        Customer customer = customerOf(id);
        customer.deactivate(reason);
        return customers.save(customer).snapshot();
    }

    public CustomerSnapshot record(Long id, ConsentStatement statement) {
        Customer customer = customerOf(id);
        customer.record(statement);
        return customers.save(customer).snapshot();
    }

    @Transactional(readOnly = true)
    public List<CustomerSnapshot> search(String name) {
        return matching(name).stream().map(Customer::snapshot).toList();
    }

    @Transactional(readOnly = true)
    public CustomerSnapshot findOne(Long id) {
        return customerOf(id).snapshot();
    }

    @Transactional(readOnly = true)
    public Customer reference(Long id) {
        return customerOf(id);
    }

    private List<Customer> matching(String name) {
        return Optional.ofNullable(name)
                .filter(term -> !term.isBlank())
                .map(customers::findByNameContainingIgnoreCaseOrderByNameAsc)
                .orElseGet(customers::findAllByOrderByNameAsc);
    }

    private void requireDocumentFree(CustomerDetails details, Long owner) {
        details.document()
                .flatMap(document -> customers.findByNationalIdValue(document.asText()))
                .filter(existing -> !existing.id().equals(owner))
                .ifPresent(existing -> refuseDuplicate(details));
    }

    private void refuseDuplicate(CustomerDetails details) {
        throw new BusinessException(
                "national id %s already belongs to another customer of this clinic".formatted(details.nationalId()));
    }

    private Customer customerOf(Long id) {
        return customers.findById(id).orElseThrow(() -> new ResourceNotFoundException("Customer", id));
    }
}
