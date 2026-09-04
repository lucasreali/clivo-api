package com.example.clivoapi.configuration.capability.internal;

import com.example.clivoapi.configuration.capability.CapabilityService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/capabilities")
class CapabilityController {

    private final CapabilityService capabilities;

    CapabilityController(CapabilityService capabilities) {
        this.capabilities = capabilities;
    }

    @GetMapping
    CapabilitiesView current() {
        return CapabilitiesView.of(capabilities.current());
    }
}
