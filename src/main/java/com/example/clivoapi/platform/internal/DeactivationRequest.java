package com.example.clivoapi.platform.internal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

record DeactivationRequest(@NotBlank @Size(max = 200) String reason) {
}
