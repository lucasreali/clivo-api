package com.example.clivoapi.core.encounter.internal;

import com.example.clivoapi.core.access.AuthenticatedUser;
import com.example.clivoapi.core.access.Role;
import com.example.clivoapi.core.encounter.AttachmentDownload;
import com.example.clivoapi.core.encounter.AttachmentService;
import com.example.clivoapi.core.encounter.AttachmentSnapshot;
import com.example.clivoapi.core.encounter.FileContent;
import com.example.clivoapi.core.encounter.UploadedFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/attachments")
@Tag(name = "Attachments", description = "Files kept with the clinical record of an encounter")
class AttachmentController {

    private final AttachmentService attachments;

    AttachmentController(AttachmentService attachments) {
        this.attachments = attachments;
    }

    @Operation(operationId = "uploadAttachment", summary = "Attach a file to an encounter's clinical record")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    AttachmentView upload(
            @RequestParam UUID encounterId,
            @RequestPart MultipartFile file,
            @AuthenticationPrincipal AuthenticatedUser viewer)
            throws IOException {
        return AttachmentView.of(attachments.attach(encounterId, uploadOf(file), roleOf(viewer)));
    }

    @Operation(operationId = "listCustomerAttachments", summary = "List the files attached to a customer's encounters")
    @GetMapping
    List<AttachmentView> of(@RequestParam UUID customerId, @AuthenticationPrincipal AuthenticatedUser viewer) {
        return attachments.visibleTo(customerId, roleOf(viewer)).all().stream()
                .map(AttachmentView::of)
                .toList();
    }

    @Operation(operationId = "downloadAttachment", summary = "Download the content of an attachment")
    @GetMapping("/{id}/content")
    ResponseEntity<Resource> download(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser viewer) {
        AttachmentDownload download = attachments.download(id, roleOf(viewer));
        return ResponseEntity.ok()
                .headers(headersFor(download.details()))
                .body(new ByteArrayResource(download.content().bytes()));
    }

    private HttpHeaders headersFor(AttachmentSnapshot details) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(details.mimeType()));
        headers.setContentDisposition(ContentDisposition.attachment().filename(details.fileName()).build());
        return headers;
    }

    private UploadedFile uploadOf(MultipartFile file) throws IOException {
        return new UploadedFile(
                file.getOriginalFilename(), file.getContentType(), new FileContent(file.getBytes()));
    }

    private Role roleOf(AuthenticatedUser viewer) {
        return Optional.ofNullable(viewer).map(AuthenticatedUser::role).orElse(Role.RECEPTION);
    }
}
