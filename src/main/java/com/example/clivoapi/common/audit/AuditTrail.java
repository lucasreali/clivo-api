package com.example.clivoapi.common.audit;

import com.example.clivoapi.common.audit.internal.AuditLogRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuditTrail {

    private final AuditLogRepository entries;

    AuditTrail(AuditLogRepository entries) {
        this.entries = entries;
    }

    public List<AuditEntrySnapshot> of(AuditedRecord record) {
        return entries.findByRecordOrderByOccurredAtDesc(record).stream()
                .map(AuditEntry::snapshot)
                .toList();
    }
}
