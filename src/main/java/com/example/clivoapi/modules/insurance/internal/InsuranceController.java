package com.example.clivoapi.modules.insurance.internal;

import com.example.clivoapi.common.extension.RequiresModule;
import com.example.clivoapi.modules.insurance.InsuranceService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiresModule("insurance")
@Tag(name = "Insurance", description = "Health plans and the customers enrolled in them. Requires the `insurance` module")
class InsuranceController {

    private final InsuranceService insurance;

    InsuranceController(InsuranceService insurance) {
        this.insurance = insurance;
    }

    @Operation(operationId = "registerInsurancePlan", summary = "Register an insurance plan")
    @PostMapping("/api/insurance-plans")
    @ResponseStatus(HttpStatus.CREATED)
    PlanView register(@Valid @RequestBody PlanRequest request) {
        return PlanView.of(insurance.register(request.toDetails()));
    }

    @Operation(operationId = "listInsurancePlans", summary = "List the insurance plans")
    @GetMapping("/api/insurance-plans")
    List<PlanView> catalogue() {
        return insurance.catalogue().stream().map(PlanView::of).toList();
    }

    @Operation(operationId = "describeInsurancePlan", summary = "Redescribe an insurance plan")
    @PutMapping("/api/insurance-plans/{id}")
    PlanView describe(@PathVariable UUID id, @Valid @RequestBody PlanRequest request) {
        return PlanView.of(insurance.describe(id, request.toDetails()));
    }

    @Operation(operationId = "deactivateInsurancePlan", summary = "Deactivate an insurance plan")
    @PostMapping("/api/insurance-plans/{id}/deactivation")
    PlanView deactivate(@PathVariable UUID id) {
        return PlanView.of(insurance.deactivate(id));
    }

    @Operation(operationId = "enrolInsuranceMembership", summary = "Enrol a customer in a plan under a member number")
    @PostMapping("/api/insurance-memberships")
    @ResponseStatus(HttpStatus.CREATED)
    MembershipView enrol(@Valid @RequestBody MembershipRequest request) {
        return MembershipView.of(
                insurance.enrol(request.customerId(), request.planId(), request.toMemberNumber()));
    }

    @Operation(operationId = "listCustomerInsuranceMemberships", summary = "List a customer's memberships")
    @GetMapping("/api/insurance-memberships")
    List<MembershipView> membershipsOf(@RequestParam UUID customerId) {
        return insurance.membershipsOf(customerId).stream().map(MembershipView::of).toList();
    }
}
