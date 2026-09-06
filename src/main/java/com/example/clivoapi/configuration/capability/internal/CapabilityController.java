package com.example.clivoapi.configuration.capability.internal;

import com.example.clivoapi.configuration.capability.CapabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/capabilities")
@Tag(name = "Capabilities", description = "What the current clinic is allowed to do, given the modules it activated")
class CapabilityController {

    private final CapabilityService capabilities;

    CapabilityController(CapabilityService capabilities) {
        this.capabilities = capabilities;
    }

    @Operation(operationId = "getCapabilities", summary = "Report the features the current clinic can use")
    @GetMapping
    CapabilitiesView current() {
        return CapabilitiesView.of(capabilities.current());
    }
}
