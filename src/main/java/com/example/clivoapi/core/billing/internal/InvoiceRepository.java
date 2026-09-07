package com.example.clivoapi.core.billing.internal;

import com.example.clivoapi.core.billing.Invoice;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    List<Invoice> findByCustomerIdOrderByIdDesc(UUID customerId);

    Optional<Invoice> findByEncounterId(UUID encounterId);

    List<Invoice> findByDueDateBetweenOrderByDueDateAsc(LocalDate from, LocalDate to);
}
