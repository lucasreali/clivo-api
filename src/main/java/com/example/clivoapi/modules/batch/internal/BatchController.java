package com.example.clivoapi.modules.batch.internal;

import com.example.clivoapi.common.extension.RequiresModule;
import com.example.clivoapi.modules.batch.BatchService;
import com.example.clivoapi.modules.batch.BatchSnapshot;
import com.example.clivoapi.modules.inventory.Quantity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
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
@Tag(name = "Batches", description = "Batch and expiry control over stock. Requires the `batch` module")
class BatchController {

    private final BatchService batches;

    BatchController(BatchService batches) {
        this.batches = batches;
    }

    @Operation(operationId = "receiveBatch", summary = "Receive a batch of a product")
    @PostMapping("/api/products/{productId}/batches")
    @ResponseStatus(HttpStatus.CREATED)
    BatchView receive(@PathVariable UUID productId, @Valid @RequestBody BatchRequest request) {
        return BatchView.of(batches.receive(productId, request.toDetails()));
    }

    @Operation(operationId = "listProductBatches", summary = "List a product's batches")
    @GetMapping("/api/products/{productId}/batches")
    List<BatchView> of(@PathVariable UUID productId) {
        return viewsOf(batches.of(productId));
    }

    @Operation(operationId = "selectBatchForDispatch", summary = "Choose the batch that should serve a quantity, nearest expiry first")
    @GetMapping("/api/products/{productId}/batches/selection")
    BatchChoiceView selectFor(@PathVariable UUID productId, @RequestParam BigDecimal quantity) {
        return BatchChoiceView.of(batches.selectFor(productId, new Quantity(quantity)));
    }

    @Operation(operationId = "listExpiringBatches", summary = "List the batches expiring within a number of days")
    @GetMapping("/api/batches/expiring")
    List<BatchView> expiring(@RequestParam(required = false) Integer days) {
        return viewsOf(batches.expiringWithin(days));
    }

    @Operation(operationId = "listBatchesAwaitingDiscard", summary = "List the expired batches still awaiting discard")
    @GetMapping("/api/batches/awaiting-discard")
    List<BatchView> awaitingDiscard() {
        return viewsOf(batches.awaitingDiscard());
    }

    @Operation(operationId = "getBatch", summary = "Read one batch")
    @GetMapping("/api/batches/{id}")
    BatchView findOne(@PathVariable UUID id) {
        return BatchView.of(batches.findOne(id));
    }

    @Operation(operationId = "discardBatch", summary = "Discard a batch, stating the reason")
    @PostMapping("/api/batches/{id}/discard")
    BatchView discard(@PathVariable UUID id, @RequestBody DiscardRequest request) {
        return BatchView.of(batches.discard(id, request.toReason()));
    }

    private List<BatchView> viewsOf(List<BatchSnapshot> found) {
        return found.stream().map(BatchView::of).toList();
    }
}
