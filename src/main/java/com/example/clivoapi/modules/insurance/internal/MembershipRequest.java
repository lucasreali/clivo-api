package com.example.clivoapi.modules.insurance.internal;

import com.example.clivoapi.modules.insurance.MemberNumber;
import jakarta.validation.constraints.NotNull;

record MembershipRequest(@NotNull Long customerId, @NotNull Long planId, String memberNumber) {

    MemberNumber toMemberNumber() {
        return new MemberNumber(memberNumber);
    }
}
