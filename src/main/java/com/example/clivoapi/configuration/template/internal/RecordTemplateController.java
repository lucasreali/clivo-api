package com.example.clivoapi.configuration.template.internal;

import com.example.clivoapi.configuration.template.RecordTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/record-templates")
@Tag(name = "Record templates", description = "Clinical record layouts a clinic composes for itself")
class RecordTemplateController {

    private final RecordTemplateService templates;

    RecordTemplateController(RecordTemplateService templates) {
        this.templates = templates;
    }

    @Operation(operationId = "listRecordTemplates", summary = "List the clinic's record templates")
    @GetMapping
    List<RecordTemplateView> list() {
        return templates.findAll().stream().map(RecordTemplateView::of).toList();
    }

    @Operation(operationId = "getRecordTemplate", summary = "Read one record template with its sections and fields")
    @GetMapping("/{id}")
    RecordTemplateView findOne(@PathVariable Long id) {
        return RecordTemplateView.of(templates.findOne(id));
    }

    @Operation(operationId = "draftRecordTemplate", summary = "Draft a new record template")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    RecordTemplateView draft(@Valid @RequestBody RecordTemplateRequest request) {
        return RecordTemplateView.of(
                templates.draft(request.name(), request.requiresModule(), request.content()));
    }

    @Operation(operationId = "redefineRecordTemplate", summary = "Redefine a draft's content, leaving published versions untouched")
    @PutMapping("/{id}")
    RecordTemplateView redefine(@PathVariable Long id, @Valid @RequestBody RecordTemplateRequest request) {
        return RecordTemplateView.of(templates.redefine(id, request.content()));
    }

    @Operation(operationId = "publishRecordTemplate", summary = "Publish a template so encounters may use it")
    @PostMapping("/{id}/publication")
    RecordTemplateView publish(@PathVariable Long id) {
        return RecordTemplateView.of(templates.publish(id));
    }
}
