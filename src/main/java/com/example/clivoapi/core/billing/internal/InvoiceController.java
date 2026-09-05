package com.example.clivoapi.core.billing.internal;

import com.example.clivoapi.core.billing.BillingService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/invoices")
class InvoiceController {

    private final BillingService billing;

    InvoiceController(BillingService billing) {
        this.billing = billing;
    }

    @GetMapping
    List<InvoiceView> byCustomer(@RequestParam Long customerId) {
        return billing.findByCustomer(customerId).stream().map(InvoiceView::of).toList();
    }

    @GetMapping("/{id}")
    InvoiceView findOne(@PathVariable Long id) {
        return InvoiceView.of(billing.findOne(id));
    }

    @PostMapping("/{id}/discount")
    InvoiceView applyDiscount(@PathVariable Long id, @Valid @RequestBody DiscountRequest request) {
        return InvoiceView.of(billing.applyDiscount(id, request.toAmount(), request.toReason()));
    }
}
