package com.example.clivoapi.core.customer.internal;

import com.example.clivoapi.core.customer.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers")
@Tag(name = "Customers", description = "The people the clinic attends")
class CustomerController {

    private final CustomerService customers;

    CustomerController(CustomerService customers) {
        this.customers = customers;
    }

    @Operation(operationId = "registerCustomer", summary = "Register a customer")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    CustomerView register(@Valid @RequestBody CustomerRequest request) {
        return CustomerView.of(customers.register(request.toDetails()));
    }

    @Operation(operationId = "searchCustomers", summary = "Search customers by name")
    @GetMapping
    List<CustomerView> search(@RequestParam(required = false) String name) {
        return customers.search(name).stream().map(CustomerView::of).toList();
    }

    @Operation(operationId = "getCustomer", summary = "Read one customer")
    @GetMapping("/{id}")
    CustomerView findOne(@PathVariable UUID id) {
        return CustomerView.of(customers.findOne(id));
    }

    @Operation(operationId = "describeCustomer", summary = "Redescribe a customer")
    @PutMapping("/{id}")
    CustomerView describe(@PathVariable UUID id, @Valid @RequestBody CustomerRequest request) {
        return CustomerView.of(customers.describe(id, request.toDetails()));
    }

    @Operation(operationId = "deactivateCustomer", summary = "Deactivate a customer, stating the reason")
    @PostMapping("/{id}/deactivation")
    CustomerView deactivate(@PathVariable UUID id, @RequestBody DeactivationRequest request) {
        return CustomerView.of(customers.deactivate(id, request.toReason()));
    }

    @Operation(operationId = "recordCustomerConsent", summary = "Record a consent statement, appended to the customer's history")
    @PostMapping("/{id}/consents")
    @ResponseStatus(HttpStatus.CREATED)
    CustomerView record(@PathVariable UUID id, @Valid @RequestBody ConsentRequest request) {
        return CustomerView.of(customers.record(id, request.toStatement()));
    }
}
