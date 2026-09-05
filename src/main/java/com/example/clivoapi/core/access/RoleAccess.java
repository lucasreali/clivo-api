package com.example.clivoapi.core.access;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.extension.ClinicParameters;
import com.example.clivoapi.common.extension.ParameterCode;
import com.example.clivoapi.common.extension.RoleAccessPolicy;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class RoleAccess {

    private static final ParameterCode ROLE_MODEL = new ParameterCode("role_model");

    private final Map<String, RoleAccessPolicy> policies;
    private final ClinicParameters parameters;

    RoleAccess(Map<String, RoleAccessPolicy> policies, ClinicParameters parameters) {
        this.policies = Map.copyOf(policies);
        this.parameters = parameters;
    }

    public boolean allowsClinicalRecord(Role role) {
        return currentPolicy().allowsClinicalRecord(role.asViewer());
    }

    private RoleAccessPolicy currentPolicy() {
        String roleModel = parameters.valueOf(ROLE_MODEL).asText();
        return Optional.ofNullable(policies.get(roleModel))
                .orElseThrow(() -> new BusinessException(
                        "no access policy is registered for role model %s".formatted(roleModel)));
    }
}
