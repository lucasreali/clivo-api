package com.example.clivoapi.core.access.internal;

import com.example.clivoapi.core.access.HashedPassword;
import com.example.clivoapi.core.access.PasswordHashing;
import com.example.clivoapi.core.access.RawPassword;
import org.springframework.security.crypto.password.PasswordEncoder;

class EncodedPasswordHashing implements PasswordHashing {

    private final PasswordEncoder encoder;

    EncodedPasswordHashing(PasswordEncoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public HashedPassword hash(RawPassword password) {
        return new HashedPassword(encoder.encode(password.asText()));
    }

    @Override
    public boolean matches(RawPassword password, HashedPassword hashed) {
        return encoder.matches(password.asText(), hashed.asText());
    }
}
