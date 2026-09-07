@ApplicationModule(allowedDependencies = {
    "common",
    "core :: access",
    "configuration :: modules",
    "configuration :: parameter"
})
package com.example.clivoapi.platform;

import org.springframework.modulith.ApplicationModule;
