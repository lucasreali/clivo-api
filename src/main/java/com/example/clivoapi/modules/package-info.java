@ApplicationModule(allowedDependencies = {
    "common",
    "configuration",
    "patterns",
    "core :: billing",
    "core :: catalog",
    "core :: customer",
    "core :: encounter",
    "core :: practitioner",
    "core :: scheduling"
})
package com.example.clivoapi.modules;

import org.springframework.modulith.ApplicationModule;
