package com.example.clivoapi.core.access.internal;

import com.example.clivoapi.core.access.EmailAddress;
import com.example.clivoapi.core.access.RawPassword;
import com.example.clivoapi.core.access.SignInAttempt;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

record SignInRequest(@NotBlank @Email String email, @NotBlank String password) {

    SignInAttempt toAttempt() {
        return new SignInAttempt(new EmailAddress(email), new RawPassword(password));
    }
}
