@ApplicationModule(allowedDependencies = {
    "common",
    "configuration",
    "patterns",
    "core :: customer",
    "core :: encounter"
})
package com.example.clivoapi.modules;

import org.springframework.modulith.ApplicationModule;
