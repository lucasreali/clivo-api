@ApplicationModule(allowedDependencies = {
    "common",
    "core :: access",
    "core :: practitioner",
    "core :: scheduling",
    "configuration :: modules",
    "configuration :: template"
})
package com.example.clivoapi.patterns;

import org.springframework.modulith.ApplicationModule;
