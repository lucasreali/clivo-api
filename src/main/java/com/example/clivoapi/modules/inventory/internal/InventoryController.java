package com.example.clivoapi.modules.inventory.internal;

import com.example.clivoapi.common.extension.RequiresModule;
import com.example.clivoapi.modules.inventory.InventoryService;
import com.example.clivoapi.modules.inventory.ProductSnapshot;
import jakarta.validation.Valid;
import java.util.List;
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
class InventoryController {

    private final InventoryService inventory;

    InventoryController(InventoryService inventory) {
        this.inventory = inventory;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ProductView register(@Valid @RequestBody ProductRequest request) {
        return ProductView.of(inventory.register(request.toDetails()));
    }

    @GetMapping
    List<ProductView> catalogue(@RequestParam(defaultValue = "false") boolean belowMinimum) {
        return listed(belowMinimum).stream().map(ProductView::of).toList();
    }

    @GetMapping("/{id}")
    ProductView findOne(@PathVariable Long id) {
        return ProductView.of(inventory.findOne(id));
    }

    @PutMapping("/{id}")
    ProductView describe(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return ProductView.of(inventory.describe(id, request.toDetails()));
    }

    @PostMapping("/{id}/deactivation")
    ProductView deactivate(@PathVariable Long id) {
        return ProductView.of(inventory.deactivate(id));
    }

    @PostMapping("/{id}/movements")
    @ResponseStatus(HttpStatus.CREATED)
    ProductView move(@PathVariable Long id, @Valid @RequestBody StockEntryRequest request) {
        return ProductView.of(inventory.move(id, request.toEntry()));
    }

    @GetMapping("/{id}/movements")
    List<StockMovementView> historyOf(@PathVariable Long id) {
        return inventory.historyOf(id).stream().map(StockMovementView::of).toList();
    }

    private List<ProductSnapshot> listed(boolean belowMinimum) {
        return belowMinimum ? inventory.belowMinimum() : inventory.catalogue();
    }
}
