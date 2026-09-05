@ApplicationModule(allowedDependencies = {
    "common",
    "configuration",
    "patterns",
    "core :: billing",
    "core :: catalog",
    "core :: customer",
    "core :: encounter"
})
package com.example.clivoapi.modules;

import org.springframework.modulith.ApplicationModule;
