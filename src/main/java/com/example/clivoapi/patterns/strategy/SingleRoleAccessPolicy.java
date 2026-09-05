package com.example.clivoapi.patterns.strategy;

import com.example.clivoapi.common.extension.RoleAccessPolicy;
import com.example.clivoapi.common.extension.ViewerRole;
import org.springframework.stereotype.Component;

@Component(SingleRoleAccessPolicy.SINGLE)
public class SingleRoleAccessPolicy implements RoleAccessPolicy {

    public static final String SINGLE = "SINGLE";

    @Override
    public boolean allowsClinicalRecord(ViewerRole viewer) {
        return true;
    }
}
