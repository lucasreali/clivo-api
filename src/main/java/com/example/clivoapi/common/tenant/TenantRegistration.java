package com.example.clivoapi.common.tenant;

import com.example.clivoapi.common.exception.BusinessException;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public record TenantRegistration(String code, String name, TenantProfile profile) {

    private static final Pattern CODE_SHAPE = Pattern.compile("[A-Z0-9][A-Z0-9-]{1,19}");

    public TenantRegistration {
        code = validCode(code);
        name = requiredName(name);
        profile = Objects.requireNonNullElseGet(profile, TenantProfile::unknown);
    }

    static String validCode(String code) {
        String candidate = code == null ? "" : code.trim().toUpperCase(Locale.ROOT);
        if (CODE_SHAPE.matcher(candidate).matches()) {
            return candidate;
        }
        throw new BusinessException("a clinic code holds 2 to 20 letters, digits or hyphens");
    }

    static String requiredName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessException("a clinic name is required");
        }
        return name.trim();
    }
}
