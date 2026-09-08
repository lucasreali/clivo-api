package com.example.clivoapi.core.encounter;

import com.example.clivoapi.common.extension.ChargeLedger;
import com.example.clivoapi.common.extension.CoverageDirectory;
import com.example.clivoapi.common.extension.CoverageNote;
import com.example.clivoapi.common.extension.EncounterCharge;
import com.example.clivoapi.common.extension.EncounterCharges;
import com.example.clivoapi.core.access.Role;
import com.example.clivoapi.core.customer.CustomerService;
import com.example.clivoapi.core.encounter.internal.EncounterRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
public class CustomerHistoryAssembler {

    private final EncounterRepository encounters;
    private final CustomerService customers;
    private final ChargeLedger ledger;
    private final CoverageDirectories coverage;
    private final ClinicalDisclosure disclosure;

    CustomerHistoryAssembler(
            EncounterRepository encounters,
            CustomerService customers,
            ChargeLedger ledger,
            List<CoverageDirectory> directories,
            ClinicalDisclosure disclosure) {
        this.encounters = encounters;
        this.customers = customers;
        this.ledger = ledger;
        this.coverage = new CoverageDirectories(directories);
        this.disclosure = disclosure;
    }

    public CustomerHistory assemble(UUID customerId, Role viewer) {
        List<Encounter> attended = encounters.findByCustomerIdOrderByStartedAtDesc(customerId);
        EncounterCharges charges = ledger.chargesOf(identifiersOf(attended));
        List<HistoryEntry> entries = entriesOf(attended, charges, customerId, viewer);
        return new CustomerHistory(
                customerId,
                customers.findOne(customerId).registeredAt(),
                entries,
                EncounterCounts.of(entries),
                FinancialSummary.of(charges),
                disclosure.alertsOf(customerId, viewer).orElse(null));
    }

    private List<HistoryEntry> entriesOf(
            List<Encounter> attended, EncounterCharges charges, UUID customerId, Role viewer) {
        Optional<CustomerAttachments> files = disclosure.filesOf(customerId, viewer);
        return attended.stream()
                .map(encounter -> entryOf(encounter, charges.of(encounter.id()), files, customerId, viewer))
                .toList();
    }

    private HistoryEntry entryOf(
            Encounter encounter,
            EncounterCharge charge,
            Optional<CustomerAttachments> files,
            UUID customerId,
            Role viewer) {
        return new HistoryEntry(
                disclosure.listedFor(encounter, viewer),
                charge,
                insuranceBehind(charge, customerId).orElse(null),
                files.map(kept -> kept.of(charge.encounterId())).orElse(null));
    }

    private Optional<CoverageNote> insuranceBehind(EncounterCharge charge, UUID customerId) {
        if (charge.coveredByInsurance()) {
            return coverage.coverageOf(customerId);
        }
        return Optional.empty();
    }

    private List<UUID> identifiersOf(List<Encounter> attended) {
        return attended.stream().map(Encounter::id).toList();
    }
}
