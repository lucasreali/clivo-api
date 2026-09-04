package com.example.clivoapi.core.access.internal;

import com.example.clivoapi.core.access.AccessService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(AppUserController.PATH)
class AppUserController {

    static final String PATH = "/api/users";

    private final AccessService access;

    AppUserController(AccessService access) {
        this.access = access;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    UserView register(@Valid @RequestBody UserRegistrationRequest request) {
        return UserView.of(access.register(request.toRegistration()));
    }

    @GetMapping
    List<UserView> list() {
        return access.usersOfCurrentClinic().stream().map(UserView::of).toList();
    }
}
