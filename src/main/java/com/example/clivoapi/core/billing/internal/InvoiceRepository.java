package com.example.clivoapi.core.billing.internal;

import com.example.clivoapi.core.billing.Invoice;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    List<Invoice> findByCustomerIdOrderByIdDesc(Long customerId);

    Optional<Invoice> findByEncounterId(Long encounterId);
}
