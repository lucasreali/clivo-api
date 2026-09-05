package com.example.clivoapi.core.billing;

import com.example.clivoapi.common.exception.ResourceNotFoundException;
import com.example.clivoapi.common.extension.CompletedEncounter;
import com.example.clivoapi.common.money.Money;
import com.example.clivoapi.core.billing.internal.InvoiceRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BillingService {

    private final InvoiceRepository invoices;
    private final InvoiceAssembler assembler;

    BillingService(InvoiceRepository invoices, InvoiceAssembler assembler) {
        this.invoices = invoices;
        this.assembler = assembler;
    }

    public InvoiceSnapshot issueFor(CompletedEncounter completed) {
        return invoices.save(assembler.assemble(completed)).snapshot();
    }

    public InvoiceSnapshot applyDiscount(Long id, Money amount, DiscountReason reason) {
        Invoice invoice = invoiceOf(id);
        invoice.applyDiscount(amount, reason);
        return invoices.save(invoice).snapshot();
    }

    @Transactional(readOnly = true)
    public List<InvoiceSnapshot> findByCustomer(Long customerId) {
        return invoices.findByCustomerIdOrderByIdDesc(customerId).stream()
                .map(Invoice::snapshot)
                .toList();
    }

    @Transactional(readOnly = true)
    public InvoiceSnapshot findOne(Long id) {
        return invoiceOf(id).snapshot();
    }

    @Transactional(readOnly = true)
    public InvoiceSnapshot findByEncounter(Long encounterId) {
        return invoices.findByEncounterId(encounterId)
                .map(Invoice::snapshot)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice of encounter", encounterId));
    }

    private Invoice invoiceOf(Long id) {
        return invoices.findById(id).orElseThrow(() -> new ResourceNotFoundException("Invoice", id));
    }
}
