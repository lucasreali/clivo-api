package com.example.clivoapi.common.extension;

import com.example.clivoapi.common.exception.ForbiddenOperationException;
import com.example.clivoapi.common.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

public class RequiresModuleInterceptor implements HandlerInterceptor {

    private final ModuleActivationState activationState;
    private final ModuleGrantState grantState;

    public RequiresModuleInterceptor(ModuleActivationState activationState, ModuleGrantState grantState) {
        this.activationState = activationState;
        this.grantState = grantState;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        requirementOf(handler).ifPresent(module -> requireReachable(module, request));
        return true;
    }

    private void requireReachable(ModuleCode module, HttpServletRequest request) {
        requireActive(module, request);
        requireGranted(module);
    }

    private void requireActive(ModuleCode module, HttpServletRequest request) {
        if (activationState.isActive(module)) {
            return;
        }
        throw new ResourceNotFoundException("resource", request.getRequestURI());
    }

    private void requireGranted(ModuleCode module) {
        if (grantState.isGrantedToCaller(module)) {
            return;
        }
        throw new ForbiddenOperationException("access to module %s was not granted".formatted(module));
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
}
