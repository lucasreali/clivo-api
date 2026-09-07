package com.example.clivoapi.core.encounter;

import com.example.clivoapi.common.exception.ResourceNotFoundException;
import com.example.clivoapi.common.extension.EncounterCompletionListener;
import com.example.clivoapi.common.extension.RecordAssembly;
import com.example.clivoapi.common.extension.RecordValues;
import com.example.clivoapi.common.extension.StockDispenser;
import com.example.clivoapi.core.access.Role;
import com.example.clivoapi.core.access.RoleAccess;
import com.example.clivoapi.core.encounter.internal.EncounterRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EncounterService {

    private final EncounterRepository encounters;
    private final EncounterAssembler assembler;
    private final RecordAssembly records;
    private final RoleAccess roleAccess;
    private final EncounterCompletion completion;
    private final EncounterSupplies supplies;

    EncounterService(
            EncounterRepository encounters,
            EncounterAssembler assembler,
            RecordAssembly records,
            RoleAccess roleAccess,
            List<EncounterCompletionListener> listeners,
            List<StockDispenser> dispensers) {
        this.encounters = encounters;
        this.assembler = assembler;
        this.records = records;
        this.roleAccess = roleAccess;
        this.completion = new EncounterCompletion(listeners);
        this.supplies = new EncounterSupplies(dispensers);
    }

    public void useSupplies(UUID id, UUID productId, BigDecimal quantity) {
        supplies.dispense(encounterOf(id).consume(productId, quantity));
    }

    public EncounterSnapshot open(EncounterOpening opening) {
        return snapshotOf(encounters.save(assembler.assemble(opening)));
    }

    public EncounterSnapshot fill(UUID id, RecordValues values) {
        Encounter encounter = encounterOf(id);
        encounter.fill(values);
        return snapshotOf(encounters.save(encounter));
    }

    public EncounterSnapshot complete(UUID id, Role viewer) {
        Encounter encounter = encounterOf(id);
        records.validate(encounter.filling());
        encounter.complete();
        Encounter completed = encounters.save(encounter);
        completion.announce(completed.completion());
        return visibleTo(completed, viewer);
    }

    @Transactional(readOnly = true)
    public EncounterSnapshot findOne(UUID id, Role viewer) {
        return visibleTo(encounterOf(id), viewer);
    }

    @Transactional(readOnly = true)
    public List<EncounterSnapshot> historyOf(UUID customerId, Role viewer) {
        List<Encounter> history = encounters.findByCustomerIdOrderByStartedAtDesc(customerId);
        if (roleAccess.allowsClinicalRecord(viewer)) {
            return history.stream().map(this::snapshotOf).toList();
        }
        return history.stream().map(Encounter::summary).toList();
    }

    private EncounterSnapshot visibleTo(Encounter encounter, Role viewer) {
        if (roleAccess.allowsClinicalRecord(viewer)) {
            return snapshotOf(encounter);
        }
        return encounter.summary();
    }

    private EncounterSnapshot snapshotOf(Encounter encounter) {
        return encounter.snapshotWith(records.assemble(encounter.filling()));
    }

    private Encounter encounterOf(UUID id) {
        return encounters.findById(id).orElseThrow(() -> new ResourceNotFoundException("Encounter", id));
    }
}
