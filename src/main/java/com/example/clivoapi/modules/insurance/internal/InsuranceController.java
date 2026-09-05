package com.example.clivoapi.modules.insurance.internal;

import com.example.clivoapi.common.extension.RequiresModule;
import com.example.clivoapi.modules.insurance.InsuranceService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiresModule("insurance")
class InsuranceController {

    private final InsuranceService insurance;

    InsuranceController(InsuranceService insurance) {
        this.insurance = insurance;
    }

    @PostMapping("/api/insurance-plans")
    @ResponseStatus(HttpStatus.CREATED)
    PlanView register(@Valid @RequestBody PlanRequest request) {
        return PlanView.of(insurance.register(request.toDetails()));
    }

    @GetMapping("/api/insurance-plans")
    List<PlanView> catalogue() {
        return insurance.catalogue().stream().map(PlanView::of).toList();
    }

    @PutMapping("/api/insurance-plans/{id}")
    PlanView describe(@PathVariable Long id, @Valid @RequestBody PlanRequest request) {
        return PlanView.of(insurance.describe(id, request.toDetails()));
    }

    @PostMapping("/api/insurance-plans/{id}/deactivation")
    PlanView deactivate(@PathVariable Long id) {
        return PlanView.of(insurance.deactivate(id));
    }

    @PostMapping("/api/insurance-memberships")
    @ResponseStatus(HttpStatus.CREATED)
    MembershipView enrol(@Valid @RequestBody MembershipRequest request) {
        return MembershipView.of(
                insurance.enrol(request.customerId(), request.planId(), request.toMemberNumber()));
    }

    @GetMapping("/api/insurance-memberships")
    List<MembershipView> membershipsOf(@RequestParam Long customerId) {
        return insurance.membershipsOf(customerId).stream().map(MembershipView::of).toList();
    }
}
