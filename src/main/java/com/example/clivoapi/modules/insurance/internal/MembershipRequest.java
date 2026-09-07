package com.example.clivoapi.modules.insurance.internal;

import com.example.clivoapi.modules.insurance.MemberNumber;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

record MembershipRequest(
@NotNull UUID customerId, @NotNull UUID planId, String memberNumber) {

    MemberNumber toMemberNumber() {
        return new MemberNumber(memberNumber);
    }
}
