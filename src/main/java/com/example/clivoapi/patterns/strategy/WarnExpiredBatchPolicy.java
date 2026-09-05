package com.example.clivoapi.patterns.strategy;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.extension.BatchCandidate;
import com.example.clivoapi.common.extension.BatchCandidates;
import com.example.clivoapi.common.extension.BatchChoice;
import com.example.clivoapi.common.extension.ExpiredBatchPolicy;
import org.springframework.stereotype.Component;

@Component(WarnExpiredBatchPolicy.WARN)
public class WarnExpiredBatchPolicy implements ExpiredBatchPolicy {

    public static final String WARN = "false";

    @Override
    public BatchChoice chooseFrom(BatchCandidates candidates) {
        return candidates.firstFresh().map(BatchChoice::accepted).orElseGet(() -> underWarning(candidates));
    }

    private BatchChoice underWarning(BatchCandidates candidates) {
        return candidates.first().map(this::warnedChoiceOf).orElseThrow(() -> emptyShelfOf(candidates));
    }

    private BatchChoice warnedChoiceOf(BatchCandidate candidate) {
        return BatchChoice.warned(
                candidate, "batch %s expired on %s".formatted(candidate.code(), candidate.expiresOn()));
    }

    private BusinessException emptyShelfOf(BatchCandidates candidates) {
        return new BusinessException("no batch of %s is available".formatted(candidates.productName()));
    }
}
