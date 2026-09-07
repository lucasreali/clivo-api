package com.example.clivoapi.platform.internal;

import com.example.clivoapi.core.access.EmailAddress;
import com.example.clivoapi.core.access.RawPassword;
import com.example.clivoapi.core.access.Role;
import com.example.clivoapi.core.access.UserRegistration;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

record PlatformAdministratorRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8) String password) {

    UserRegistration toRegistration() {
        return new UserRegistration(
                name, new EmailAddress(email), new RawPassword(password), Role.PLATFORM_ADMIN);
    }
}
