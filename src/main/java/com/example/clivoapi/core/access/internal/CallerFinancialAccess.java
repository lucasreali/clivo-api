package com.example.clivoapi.core.access.internal;

import com.example.clivoapi.common.exception.ForbiddenOperationException;
import com.example.clivoapi.common.extension.FinancialAccess;
import com.example.clivoapi.core.access.AuthenticatedUser;
import com.example.clivoapi.core.access.Role;
import com.example.clivoapi.core.access.RoleAccess;
import org.springframework.stereotype.Component;

@Component
class CallerFinancialAccess implements FinancialAccess {

    private final RoleAccess roleAccess;

    CallerFinancialAccess(RoleAccess roleAccess) {
        this.roleAccess = roleAccess;
    }

    @Override
    public void requireReporting() {
        roleAccess.requireFinancialReport(callerRole());
    }

    private Role callerRole() {
        return SignedInCaller.current()
                .map(AuthenticatedUser::role)
                .orElseThrow(() -> new ForbiddenOperationException("only a signed-in user reads financial figures"));
    }
}
