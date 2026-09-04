package com.example.clivoapi.core.access;

public interface PasswordHashing {

    HashedPassword hash(RawPassword password);

    boolean matches(RawPassword password, HashedPassword hashed);
}
