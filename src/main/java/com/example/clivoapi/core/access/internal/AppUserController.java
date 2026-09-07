package com.example.clivoapi.core.access.internal;

import com.example.clivoapi.core.access.AccessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(AppUserController.PATH)
@Tag(name = "Users", description = "The people who sign in to the clinic")
class AppUserController {

    static final String PATH = "/api/users";

    private final AccessService access;

    AppUserController(AccessService access) {
        this.access = access;
    }

    @Operation(operationId = "registerUser", summary = "Register a user in the current clinic")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    UserView register(@Valid @RequestBody UserRegistrationRequest request) {
        return UserView.of(access.register(request.toRegistration()));
    }

    @Operation(operationId = "changeUserRole", summary = "Change the role of a user of the current clinic")
    @PutMapping("/{userId}/role")
    UserView changeRole(@PathVariable UUID userId, @Valid @RequestBody RoleChangeRequest request) {
        return UserView.of(access.changeRole(userId, request.toRole()));
    }

    @Operation(operationId = "deactivateUser", summary = "Deactivate a user of the current clinic")
    @PostMapping("/{userId}/deactivation")
    UserView deactivate(@PathVariable UUID userId) {
        return UserView.of(access.deactivate(userId));
    }

    @Operation(operationId = "listUsers", summary = "List the users of the current clinic")
    @GetMapping
    List<UserView> list() {
        return access.usersOfCurrentClinic().stream().map(UserView::of).toList();
    }
}
