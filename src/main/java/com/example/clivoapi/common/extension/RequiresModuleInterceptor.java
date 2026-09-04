package com.example.clivoapi.common.extension;

import com.example.clivoapi.common.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

public class RequiresModuleInterceptor implements HandlerInterceptor {

    private final ModuleActivationState activationState;

    public RequiresModuleInterceptor(ModuleActivationState activationState) {
        this.activationState = activationState;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        requirementOf(handler)
                .filter(this::isInactive)
                .ifPresent(module -> hide(request));
        return true;
    }

    private Optional<ModuleCode> requirementOf(Object handler) {
        if (handler instanceof HandlerMethod method) {
            return declarationOn(method).map(RequiresModule::value).map(ModuleCode::new);
        }
        return Optional.empty();
    }

    private Optional<RequiresModule> declarationOn(HandlerMethod method) {
        return Optional.ofNullable(method.getMethodAnnotation(RequiresModule.class))
                .or(() -> Optional.ofNullable(method.getBeanType().getAnnotation(RequiresModule.class)));
    }

    private boolean isInactive(ModuleCode module) {
        return !activationState.isActive(module);
    }

    private void hide(HttpServletRequest request) {
        throw new ResourceNotFoundException("resource", request.getRequestURI());
    }
}
