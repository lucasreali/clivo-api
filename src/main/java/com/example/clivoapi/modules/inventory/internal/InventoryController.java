package com.example.clivoapi.modules.inventory.internal;

import com.example.clivoapi.common.extension.RequiresModule;
import com.example.clivoapi.modules.inventory.InventoryService;
import com.example.clivoapi.modules.inventory.ProductSnapshot;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@RequiresModule("inventory")
@Tag(name = "Inventory", description = "Products and stock movements. Requires the `inventory` module")
class InventoryController {

    private final InventoryService inventory;

    InventoryController(InventoryService inventory) {
        this.inventory = inventory;
    }

    @Operation(operationId = "registerProduct", summary = "Register a product")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ProductView register(@Valid @RequestBody ProductRequest request) {
        return ProductView.of(inventory.register(request.toDetails()));
    }

    @Operation(operationId = "listProducts", summary = "List the products, optionally only those below the minimum")
    @GetMapping
    List<ProductView> catalogue(@RequestParam(defaultValue = "false") boolean belowMinimum) {
        return listed(belowMinimum).stream().map(ProductView::of).toList();
    }

    @Operation(operationId = "getProduct", summary = "Read one product with its current balance")
    @GetMapping("/{id}")
    ProductView findOne(@PathVariable UUID id) {
        return ProductView.of(inventory.findOne(id));
    }

    @Operation(operationId = "describeProduct", summary = "Redescribe a product")
    @PutMapping("/{id}")
    ProductView describe(@PathVariable UUID id, @Valid @RequestBody ProductRequest request) {
        return ProductView.of(inventory.describe(id, request.toDetails()));
    }

    @Operation(operationId = "deactivateProduct", summary = "Deactivate a product, keeping its history")
    @PostMapping("/{id}/deactivation")
    ProductView deactivate(@PathVariable UUID id) {
        return ProductView.of(inventory.deactivate(id));
    }

    @Operation(operationId = "moveStock", summary = "Move stock in or out of a product")
    @PostMapping("/{id}/movements")
    @ResponseStatus(HttpStatus.CREATED)
    ProductView move(@PathVariable UUID id, @Valid @RequestBody StockEntryRequest request) {
        return ProductView.of(inventory.move(id, request.toEntry()));
    }

    @Operation(operationId = "listStockMovements", summary = "List a product's stock movements")
    @GetMapping("/{id}/movements")
    List<StockMovementView> historyOf(@PathVariable UUID id) {
        return inventory.historyOf(id).stream().map(StockMovementView::of).toList();
    }

    private List<ProductSnapshot> listed(boolean belowMinimum) {
        return belowMinimum ? inventory.belowMinimum() : inventory.catalogue();
    }
}
