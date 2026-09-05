package com.example.clivoapi.common.extension;

public interface ExpiredBatchPolicy {

    BatchChoice chooseFrom(BatchCandidates candidates);
}
