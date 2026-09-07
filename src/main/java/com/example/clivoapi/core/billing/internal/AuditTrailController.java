package com.example.clivoapi.core.billing.internal;

import com.example.clivoapi.common.audit.AuditTrail;
import com.example.clivoapi.common.audit.AuditedRecord;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit-trail")
@Tag(name = "Audit trail", description = "Who changed what, and when")
class AuditTrailController {

    private final AuditTrail trail;

    AuditTrailController(AuditTrail trail) {
        this.trail = trail;
    }

    @Operation(operationId = "getAuditTrail", summary = "Read the recorded changes of one entity record")
    @GetMapping
    List<AuditEntryView> of(@RequestParam String entity, @RequestParam UUID recordId) {
        return trail.of(new AuditedRecord(entity, recordId)).stream()
                .map(AuditEntryView::of)
                .toList();
    }
}
