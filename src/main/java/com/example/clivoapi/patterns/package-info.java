@ApplicationModule(allowedDependencies = {
    "common",
    "core :: practitioner",
    "core :: scheduling",
    "configuration :: modules",
    "configuration :: template"
})
package com.example.clivoapi.patterns;

import org.springframework.modulith.ApplicationModule;
