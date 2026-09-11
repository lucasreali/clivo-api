package com.example.clivoapi.modules.inventory;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.exception.ResourceNotFoundException;
import com.example.clivoapi.common.extension.BatchChoice;
import com.example.clivoapi.common.extension.BatchDispatcher;
import com.example.clivoapi.common.extension.SuppliesUsed;
import com.example.clivoapi.modules.inventory.internal.ProductRepository;
import com.example.clivoapi.modules.inventory.internal.StockMovementRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InventoryService {

    private final ProductRepository products;
    private final StockMovementRepository movements;
    private final AuditorAware<UUID> auditor;
    private final List<BatchDispatcher> dispatchers;

    InventoryService(
            ProductRepository products,
            StockMovementRepository movements,
            AuditorAware<UUID> auditor,
            List<BatchDispatcher> dispatchers) {
        this.products = products;
        this.movements = movements;
        this.auditor = auditor;
        this.dispatchers = List.copyOf(dispatchers);
    }

    public ProductSnapshot register(ProductDetails details) {
        return products.save(new Product(details)).snapshot();
    }

    public ProductSnapshot describe(UUID id, ProductDetails details) {
        Product product = productOf(id);
        product.describeAs(details);
        return products.save(product).snapshot();
    }

    public ProductSnapshot deactivate(UUID id) {
        Product product = productOf(id);
        product.deactivate();
        return products.save(product).snapshot();
    }

    public ProductSnapshot move(UUID productId, StockEntry entry) {
        Product product = productOf(productId);
        entry.applyTo(product);
        movements.save(new StockMovement(products.save(product), entry, author()));
        return product.snapshot();
    }

    public ProductSnapshot discardFromBatch(UUID productId, UUID batchId, StockEntry entry) {
        Product product = productOf(productId);
        entry.applyTo(product);
        movements.save(StockMovement.discarded(products.save(product), batchId, entry, author()));
        return product.snapshot();
    }

    public void dispense(SuppliesUsed supplies) {
        Product product = productOf(supplies.productId());
        Quantity quantity = new Quantity(supplies.quantity());
        UUID batchId = batchFor(supplies).orElse(null);
        product.decreaseStock(quantity);
        movements.save(StockMovement.dispensedIn(
                products.save(product), supplies.encounterId(), quantity, author(), batchId));
    }

    /**
     * The batch is chosen first, so a clinic whose policy refuses every expired
     * lot never reaches the balance it would have lowered.
     */
    private Optional<UUID> batchFor(SuppliesUsed supplies) {
        return dispatchers.stream()
                .map(dispatcher -> dispatcher.dispatch(supplies.productId(), supplies.quantity()))
                .flatMap(Optional::stream)
                .map(BatchChoice::batchId)
                .findFirst();
    }

    @Transactional(readOnly = true)
    public List<ProductSnapshot> catalogue() {
        return products.findAllByOrderByNameAsc().stream().map(Product::snapshot).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductSnapshot> belowMinimum() {
        return catalogue().stream().filter(ProductSnapshot::belowMinimum).toList();
    }

    @Transactional(readOnly = true)
    public ProductSnapshot findOne(UUID id) {
        return productOf(id).snapshot();
    }

    @Transactional(readOnly = true)
    public List<StockMovementSnapshot> historyOf(UUID productId) {
        return movements.findByProductIdOrderByRecordedAtDesc(productId).stream()
                .map(StockMovement::snapshot)
                .toList();
    }

    @Transactional(readOnly = true)
    public Product reference(UUID id) {
        return productOf(id);
    }

    private UUID author() {
        return auditor.getCurrentAuditor()
                .orElseThrow(() -> new BusinessException("a stock movement is recorded by an identified user"));
    }

    private Product productOf(UUID id) {
        return products.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }
}
