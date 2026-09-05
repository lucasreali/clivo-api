package com.example.clivoapi.patterns.strategy;

import com.example.clivoapi.common.extension.RoleAccessPolicy;
import com.example.clivoapi.common.extension.ViewerRole;
import com.example.clivoapi.core.access.Role;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component(SegregatedRoleAccessPolicy.SEGREGATED)
public class SegregatedRoleAccessPolicy implements RoleAccessPolicy {

    public static final String SEGREGATED = "SEGREGATED";

    private static final Set<String> CLINICAL_ROLES = Set.of(Role.PRACTITIONER.name());
    private static final Set<String> FINANCIAL_ROLES = Set.of(Role.MANAGER.name());

    @Override
    public boolean allowsClinicalRecord(ViewerRole viewer) {
        return viewer.isAnyOf(CLINICAL_ROLES);
    }

    @Override
    public boolean allowsFinancialReport(ViewerRole viewer) {
        return viewer.isAnyOf(FINANCIAL_ROLES);
    }
}
