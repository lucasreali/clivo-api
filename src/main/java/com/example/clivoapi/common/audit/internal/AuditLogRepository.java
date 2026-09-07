package com.example.clivoapi.common.audit.internal;

import com.example.clivoapi.common.audit.AuditEntry;
import com.example.clivoapi.common.audit.AuditedRecord;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditEntry, UUID> {

    List<AuditEntry> findByRecordOrderByOccurredAtDesc(AuditedRecord record);
}
