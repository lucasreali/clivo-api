package com.example.clivoapi.core.customer.internal;

import com.example.clivoapi.core.customer.CustomerService;
import jakarta.validation.Valid;
import java.util.List;
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
class CustomerController {

    private final CustomerService customers;

    CustomerController(CustomerService customers) {
        this.customers = customers;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    CustomerView register(@Valid @RequestBody CustomerRequest request) {
        return CustomerView.of(customers.register(request.toDetails()));
    }

    @GetMapping
    List<CustomerView> search(@RequestParam(required = false) String name) {
        return customers.search(name).stream().map(CustomerView::of).toList();
    }

    @GetMapping("/{id}")
    CustomerView findOne(@PathVariable Long id) {
        return CustomerView.of(customers.findOne(id));
    }

    @PutMapping("/{id}")
    CustomerView describe(@PathVariable Long id, @Valid @RequestBody CustomerRequest request) {
        return CustomerView.of(customers.describe(id, request.toDetails()));
    }

    @PostMapping("/{id}/deactivation")
    CustomerView deactivate(@PathVariable Long id, @RequestBody DeactivationRequest request) {
        return CustomerView.of(customers.deactivate(id, request.toReason()));
    }

    @PostMapping("/{id}/consents")
    @ResponseStatus(HttpStatus.CREATED)
    CustomerView record(@PathVariable Long id, @Valid @RequestBody ConsentRequest request) {
        return CustomerView.of(customers.record(id, request.toStatement()));
    }
}
