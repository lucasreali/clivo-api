package com.example.clivoapi.common.extension;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
class ModuleGuardConfiguration implements WebMvcConfigurer {

    private final ModuleActivationState activationState;

    ModuleGuardConfiguration(ModuleActivationState activationState) {
        this.activationState = activationState;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RequiresModuleInterceptor(activationState));
    }
}
