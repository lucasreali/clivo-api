package com.example.clivoapi.modules.batch.internal;

import com.example.clivoapi.common.extension.RequiresModule;
import com.example.clivoapi.modules.batch.BatchService;
import com.example.clivoapi.modules.batch.BatchSnapshot;
import com.example.clivoapi.modules.inventory.Quantity;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiresModule("batch")
class BatchController {

    private final BatchService batches;

    BatchController(BatchService batches) {
        this.batches = batches;
    }

    @PostMapping("/api/products/{productId}/batches")
    @ResponseStatus(HttpStatus.CREATED)
    BatchView receive(@PathVariable Long productId, @Valid @RequestBody BatchRequest request) {
        return BatchView.of(batches.receive(productId, request.toDetails()));
    }

    @GetMapping("/api/products/{productId}/batches")
    List<BatchView> of(@PathVariable Long productId) {
        return viewsOf(batches.of(productId));
    }

    @GetMapping("/api/products/{productId}/batches/selection")
    BatchChoiceView selectFor(@PathVariable Long productId, @RequestParam BigDecimal quantity) {
        return BatchChoiceView.of(batches.selectFor(productId, new Quantity(quantity)));
    }

    @GetMapping("/api/batches/expiring")
    List<BatchView> expiring(@RequestParam(required = false) Integer days) {
        return viewsOf(batches.expiringWithin(days));
    }

    @GetMapping("/api/batches/awaiting-discard")
    List<BatchView> awaitingDiscard() {
        return viewsOf(batches.awaitingDiscard());
    }

    @GetMapping("/api/batches/{id}")
    BatchView findOne(@PathVariable Long id) {
        return BatchView.of(batches.findOne(id));
    }

    @PostMapping("/api/batches/{id}/discard")
    BatchView discard(@PathVariable Long id, @RequestBody DiscardRequest request) {
        return BatchView.of(batches.discard(id, request.toReason()));
    }

    private List<BatchView> viewsOf(List<BatchSnapshot> found) {
        return found.stream().map(BatchView::of).toList();
    }
}
