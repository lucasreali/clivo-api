package com.example.clivoapi.patterns.strategy;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.extension.BatchCandidates;
import com.example.clivoapi.common.extension.BatchChoice;
import com.example.clivoapi.common.extension.ExpiredBatchPolicy;
import org.springframework.stereotype.Component;

@Component(BlockExpiredBatchPolicy.BLOCK)
public class BlockExpiredBatchPolicy implements ExpiredBatchPolicy {

    public static final String BLOCK = "true";

    @Override
    public BatchChoice chooseFrom(BatchCandidates candidates) {
        return candidates.firstFresh().map(BatchChoice::accepted).orElseThrow(() -> refusalFor(candidates));
    }

    private BusinessException refusalFor(BatchCandidates candidates) {
        return new BusinessException(
                "no batch of %s is within its expiry date".formatted(candidates.productName()));
    }
}
