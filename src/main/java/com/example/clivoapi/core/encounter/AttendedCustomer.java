package com.example.clivoapi.core.encounter;

import java.time.LocalDate;
import java.util.UUID;

public record AttendedCustomer(UUID id, String name, LocalDate birthDate) {
}
