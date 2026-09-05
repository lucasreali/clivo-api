package com.example.clivoapi.core.billing.internal;

import com.example.clivoapi.core.access.AuthenticatedUser;
import com.example.clivoapi.core.access.Role;
import com.example.clivoapi.core.billing.BillingService;
import com.example.clivoapi.core.billing.ReportPeriod;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
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

    @GetMapping("/report")
    BillingReportView report(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to,
            @AuthenticationPrincipal AuthenticatedUser viewer) {
        return BillingReportView.of(billing.reportOf(new ReportPeriod(from, to), roleOf(viewer)));
    }

    @GetMapping("/{id}")
    InvoiceView findOne(@PathVariable Long id) {
        return InvoiceView.of(billing.findOne(id));
    }

    @PostMapping("/{id}/discount")
    InvoiceView applyDiscount(@PathVariable Long id, @Valid @RequestBody DiscountRequest request) {
        return InvoiceView.of(billing.applyDiscount(id, request.toAmount(), request.toReason()));
    }

    @PostMapping("/{id}/payments")
    @ResponseStatus(HttpStatus.CREATED)
    InvoiceView settle(@PathVariable Long id, @Valid @RequestBody PaymentRequest request) {
        return InvoiceView.of(billing.settle(id, request.toDetails()));
    }

    @PostMapping("/{id}/payments/{paymentId}/refund")
    InvoiceView refund(
            @PathVariable Long id, @PathVariable Long paymentId, @RequestBody RefundRequest request) {
        return InvoiceView.of(billing.refund(id, paymentId, request.toReason()));
    }

    private Role roleOf(AuthenticatedUser viewer) {
        return Optional.ofNullable(viewer).map(AuthenticatedUser::role).orElse(Role.RECEPTION);
    }
}
