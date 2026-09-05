package com.example.clivoapi.modules.batch;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.exception.ResourceNotFoundException;
import com.example.clivoapi.common.extension.BatchCandidates;
import com.example.clivoapi.common.extension.BatchChoice;
import com.example.clivoapi.common.extension.ClinicParameters;
import com.example.clivoapi.common.extension.ExpiredBatchPolicy;
import com.example.clivoapi.common.extension.ParameterCode;
import com.example.clivoapi.modules.batch.internal.BatchRepository;
import com.example.clivoapi.modules.inventory.InventoryService;
import com.example.clivoapi.modules.inventory.MovementReason;
import com.example.clivoapi.modules.inventory.Product;
import com.example.clivoapi.modules.inventory.Quantity;
import com.example.clivoapi.modules.inventory.StockEntry;
import com.example.clivoapi.modules.inventory.StockMovementType;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BatchService {

    private static final ParameterCode EXPIRY_ALERT_DAYS = new ParameterCode("expiry_alert_days");
    private static final ParameterCode BLOCK_EXPIRED_BATCH = new ParameterCode("block_expired_batch");

    private final BatchRepository batches;
    private final InventoryService inventory;
    private final ClinicParameters parameters;
    private final Map<String, ExpiredBatchPolicy> policies;

    BatchService(
            BatchRepository batches,
            InventoryService inventory,
            ClinicParameters parameters,
            Map<String, ExpiredBatchPolicy> policies) {
        this.batches = batches;
        this.inventory = inventory;
        this.parameters = parameters;
        this.policies = Map.copyOf(policies);
    }

    @Transactional(readOnly = true)
    public BatchChoice selectFor(Long productId, Quantity quantity) {
        Product product = batchControlled(productId);
        return currentPolicy().chooseFrom(candidatesOf(product, quantity));
    }

    public BatchSnapshot receive(Long productId, BatchDetails details) {
        Product product = batchControlled(productId);
        Batch batch = batches.save(new Batch(product, details));
        inventory.move(productId, arrivalOf(details));
        return batch.snapshot();
    }

    public BatchSnapshot discard(Long id, MovementReason reason) {
        Batch batch = batchOf(id);
        batch.discard();
        inventory.discardFromBatch(batch.product().id(), batch.id(), discardOf(batch, reason));
        return batches.save(batch).snapshot();
    }

    @Transactional(readOnly = true)
    public List<BatchSnapshot> of(Long productId) {
        return snapshotsOf(batches.findByProductIdOrderByExpiresOnAsc(productId));
    }

    @Transactional(readOnly = true)
    public List<BatchSnapshot> expiringWithin(Integer days) {
        LocalDate limit = LocalDate.now().plusDays(alertWindow(days));
        return snapshotsOf(batches.findByStatusAndExpiresOnLessThanEqualOrderByExpiresOnAsc(
                BatchStatus.AVAILABLE, limit));
    }

    @Transactional(readOnly = true)
    public List<BatchSnapshot> awaitingDiscard() {
        return snapshotsOf(batches.findByStatusAndExpiresOnLessThanEqualOrderByExpiresOnAsc(
                BatchStatus.AVAILABLE, LocalDate.now()));
    }

    @Transactional(readOnly = true)
    public BatchSnapshot findOne(Long id) {
        return batchOf(id).snapshot();
    }

    @Transactional(readOnly = true)
    public List<Batch> usableFor(Long productId) {
        return batches.findByProductIdAndStatusOrderByExpiresOnAsc(productId, BatchStatus.AVAILABLE);
    }

    private ExpiredBatchPolicy currentPolicy() {
        String key = parameters.valueOf(BLOCK_EXPIRED_BATCH).asText();
        return Optional.ofNullable(policies.get(key))
                .orElseThrow(() -> new BusinessException(
                        "no expired batch policy is registered for block_expired_batch %s".formatted(key)));
    }

    private BatchCandidates candidatesOf(Product product, Quantity quantity) {
        return new BatchCandidates(
                product.name(),
                usableFor(product.id()).stream()
                        .filter(batch -> batch.hasAvailable(quantity))
                        .map(Batch::asCandidate)
                        .toList());
    }

    private int alertWindow(Integer days) {
        return Optional.ofNullable(days).orElseGet(() -> parameters.valueOf(EXPIRY_ALERT_DAYS).asInteger());
    }

    private StockEntry arrivalOf(BatchDetails details) {
        return new StockEntry(
                StockMovementType.INBOUND,
                details.quantity(),
                new MovementReason("batch %s received".formatted(details.code())));
    }

    private StockEntry discardOf(Batch batch, MovementReason reason) {
        return new StockEntry(StockMovementType.DISCARD, batch.quantity(), reason);
    }

    private Product batchControlled(Long productId) {
        Product product = inventory.reference(productId);
        if (product.isBatchControlled()) {
            return product;
        }
        throw new BusinessException("%s is not controlled by batch".formatted(product.name()));
    }

    private List<BatchSnapshot> snapshotsOf(List<Batch> found) {
        return found.stream().map(Batch::snapshot).toList();
    }

    private Batch batchOf(Long id) {
        return batches.findById(id).orElseThrow(() -> new ResourceNotFoundException("Batch", id));
    }
}
