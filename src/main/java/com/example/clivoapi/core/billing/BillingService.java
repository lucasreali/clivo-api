package com.example.clivoapi.core.billing;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.exception.ResourceNotFoundException;
import com.example.clivoapi.common.extension.CompletedEncounter;
import com.example.clivoapi.common.extension.InvoiceAdjuster;
import com.example.clivoapi.common.money.Money;
import com.example.clivoapi.core.access.Role;
import com.example.clivoapi.core.access.RoleAccess;
import com.example.clivoapi.core.billing.internal.InvoiceRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BillingService {

    private final InvoiceRepository invoices;
    private final InvoiceAssembler assembler;
    private final AuditorAware<UUID> auditor;
    private final RoleAccess roleAccess;
    private final InvoiceAdjustments adjustments;

    BillingService(
            InvoiceRepository invoices,
            InvoiceAssembler assembler,
            AuditorAware<UUID> auditor,
            RoleAccess roleAccess,
            List<InvoiceAdjuster> adjusters) {
        this.invoices = invoices;
        this.assembler = assembler;
        this.auditor = auditor;
        this.roleAccess = roleAccess;
        this.adjustments = new InvoiceAdjustments(adjusters);
    }

    @Transactional(readOnly = true)
    public BillingReport reportOf(ReportPeriod period, Role viewer) {
        roleAccess.requireFinancialReport(viewer);
        return BillingReport.of(period, snapshotsWithin(period));
    }

    private List<InvoiceSnapshot> snapshotsWithin(ReportPeriod period) {
        return invoices.findByDueDateBetweenOrderByDueDateAsc(period.from(), period.to()).stream()
                .map(Invoice::snapshot)
                .toList();
    }

    public InvoiceSnapshot issueFor(CompletedEncounter completed) {
        Invoice invoice = assembler.assemble(completed);
        adjustments.applyTo(invoice, completed);
        return invoices.save(invoice).snapshot();
    }

    public InvoiceSnapshot coverByPackage(UUID encounterId) {
        Invoice invoice = invoiceOfEncounter(encounterId);
        invoice.coverBy(InvoiceCoverage.SESSION_PACKAGE);
        return invoices.save(invoice).snapshot();
    }

    public InvoiceSnapshot applyDiscount(UUID id, Money amount, DiscountReason reason) {
        Invoice invoice = invoiceOf(id);
        invoice.applyDiscount(amount, reason);
        return invoices.save(invoice).snapshot();
    }

    public InvoiceSnapshot settle(UUID id, PaymentDetails details) {
        Invoice invoice = invoiceOf(id);
        invoice.settle(details, recorder());
        return invoices.save(invoice).snapshot();
    }

    public InvoiceSnapshot refund(UUID id, UUID paymentId, RefundReason reason) {
        Invoice invoice = invoiceOf(id);
        invoice.refund(paymentId, reason);
        return invoices.save(invoice).snapshot();
    }

    @Transactional(readOnly = true)
    public List<InvoiceSnapshot> findByCustomer(UUID customerId) {
        return invoices.findByCustomerIdOrderByIdDesc(customerId).stream()
                .map(Invoice::snapshot)
                .toList();
    }

    @Transactional(readOnly = true)
    public InvoiceSnapshot findOne(UUID id) {
        return invoiceOf(id).snapshot();
    }

    @Transactional(readOnly = true)
    public InvoiceSnapshot findByEncounter(UUID encounterId) {
        return invoices.findByEncounterId(encounterId)
                .map(Invoice::snapshot)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice of encounter", encounterId));
    }

    private UUID recorder() {
        return auditor.getCurrentAuditor()
                .orElseThrow(() -> new BusinessException("recording a payment requires an authenticated user"));
    }

    private Invoice invoiceOfEncounter(UUID encounterId) {
        return invoices.findByEncounterId(encounterId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice of encounter", encounterId));
    }

    private Invoice invoiceOf(UUID id) {
        return invoices.findById(id).orElseThrow(() -> new ResourceNotFoundException("Invoice", id));
    }
}
