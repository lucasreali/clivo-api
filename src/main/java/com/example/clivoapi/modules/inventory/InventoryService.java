package com.example.clivoapi.modules.inventory;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.exception.ResourceNotFoundException;
import com.example.clivoapi.common.extension.SuppliesUsed;
import com.example.clivoapi.modules.inventory.internal.ProductRepository;
import com.example.clivoapi.modules.inventory.internal.StockMovementRepository;
import java.util.List;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InventoryService {

    private final ProductRepository products;
    private final StockMovementRepository movements;
    private final AuditorAware<Long> auditor;

    InventoryService(ProductRepository products, StockMovementRepository movements, AuditorAware<Long> auditor) {
        this.products = products;
        this.movements = movements;
        this.auditor = auditor;
    }

    public ProductSnapshot register(ProductDetails details) {
        return products.save(new Product(details)).snapshot();
    }

    public ProductSnapshot describe(Long id, ProductDetails details) {
        Product product = productOf(id);
        product.describeAs(details);
        return products.save(product).snapshot();
    }

    public ProductSnapshot deactivate(Long id) {
        Product product = productOf(id);
        product.deactivate();
        return products.save(product).snapshot();
    }

    public ProductSnapshot move(Long productId, StockEntry entry) {
        Product product = productOf(productId);
        entry.applyTo(product);
        movements.save(new StockMovement(products.save(product), entry, author()));
        return product.snapshot();
    }

    public ProductSnapshot discardFromBatch(Long productId, Long batchId, StockEntry entry) {
        Product product = productOf(productId);
        entry.applyTo(product);
        movements.save(StockMovement.discarded(products.save(product), batchId, entry, author()));
        return product.snapshot();
    }

    public void dispense(SuppliesUsed supplies) {
        Product product = productOf(supplies.productId());
        Quantity quantity = new Quantity(supplies.quantity());
        product.decreaseStock(quantity);
        movements.save(
                StockMovement.dispensedIn(products.save(product), supplies.encounterId(), quantity, author()));
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
    public ProductSnapshot findOne(Long id) {
        return productOf(id).snapshot();
    }

    @Transactional(readOnly = true)
    public List<StockMovementSnapshot> historyOf(Long productId) {
        return movements.findByProductIdOrderByRecordedAtDesc(productId).stream()
                .map(StockMovement::snapshot)
                .toList();
    }

    @Transactional(readOnly = true)
    public Product reference(Long id) {
        return productOf(id);
    }

    private Long author() {
        return auditor.getCurrentAuditor()
                .orElseThrow(() -> new BusinessException("a stock movement is recorded by an identified user"));
    }

    private Product productOf(Long id) {
        return products.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }
}
