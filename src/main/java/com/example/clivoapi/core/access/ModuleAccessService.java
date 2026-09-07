package com.example.clivoapi.core.access;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.exception.ForbiddenOperationException;
import com.example.clivoapi.common.exception.ResourceNotFoundException;
import com.example.clivoapi.common.extension.ModuleActivationState;
import com.example.clivoapi.common.extension.ModuleCode;
import com.example.clivoapi.core.access.internal.AppUserRepository;
import com.example.clivoapi.core.access.internal.ModuleGrantRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ModuleAccessService {

    private final ModuleGrantRepository grants;
    private final AppUserRepository users;
    private final ModuleActivationState activation;
    private final AuditorAware<UUID> auditor;

    ModuleAccessService(
            ModuleGrantRepository grants,
            AppUserRepository users,
            ModuleActivationState activation,
            AuditorAware<UUID> auditor) {
        this.grants = grants;
        this.users = users;
        this.activation = activation;
        this.auditor = auditor;
    }

    public List<ModuleCode> grant(UUID userId, ModuleCode module) {
        AppUser user = reachableUser(userId, module);
        grants.findByUserIdAndModuleCode(userId, module.value())
                .orElseGet(() -> grants.save(new ModuleGrant(userId, module, author().orElse(null))));
        return modulesOf(user);
    }

    public List<ModuleCode> revoke(UUID userId, ModuleCode module) {
        AppUser user = reachableUser(userId, module);
        requireRevocable(user);
        grants.findByUserIdAndModuleCode(userId, module.value()).ifPresent(grants::delete);
        return modulesOf(user);
    }

    @Transactional(readOnly = true)
    public List<ModuleCode> modulesGrantedTo(UUID userId) {
        return modulesOf(userOfTheClinicOf(manager(), userId));
    }

    private AppUser reachableUser(UUID userId, ModuleCode module) {
        AppUser manager = manager();
        requireActive(module);
        return userOfTheClinicOf(manager, userId);
    }

    private List<ModuleCode> modulesOf(AppUser user) {
        return grants.findByUserIdOrderByModuleCodeAsc(user.id()).stream()
                .map(ModuleGrant::module)
                .toList();
    }

    private AppUser userOfTheClinicOf(AppUser manager, UUID userId) {
        return manager.identity().clinic()
                .flatMap(clinic -> users.findByIdAndClinicId(userId, clinic))
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    private AppUser manager() {
        return author().flatMap(users::findById)
                .filter(AppUser::managesTheClinic)
                .orElseThrow(() -> new ForbiddenOperationException("only a manager decides who reaches a module"));
    }

    private void requireActive(ModuleCode module) {
        if (activation.isActive(module)) {
            return;
        }
        throw new ResourceNotFoundException("Module", module.value());
    }

    private void requireRevocable(AppUser user) {
        if (user.keepsEveryModule()) {
            throw new BusinessException("a manager keeps access to every module active in the clinic");
        }
    }

    private Optional<UUID> author() {
        return auditor.getCurrentAuditor();
    }
}
