package com.example.clivoapi.modules.commission.internal;

import com.example.clivoapi.common.extension.RequiresModule;
import com.example.clivoapi.modules.commission.CommissionPeriod;
import com.example.clivoapi.modules.commission.CommissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiresModule("commission")
@Tag(name = "Commissions", description = "What each practitioner earns from what was billed. Requires the `commission` module")
class CommissionController {

    private final CommissionService commissions;

    CommissionController(CommissionService commissions) {
        this.commissions = commissions;
    }

    @Operation(operationId = "setCommissionRate", summary = "Set the rate a practitioner is paid at")
    @PutMapping("/api/practitioners/{practitionerId}/commission-rate")
    RateView chargeAt(@PathVariable Long practitionerId, @Valid @RequestBody RateRequest request) {
        return RateView.of(commissions.chargeAt(practitionerId, request.toRate()));
    }

    @Operation(operationId = "listCommissionRates", summary = "List the practitioners' rates")
    @GetMapping("/api/commission-rates")
    List<RateView> rates() {
        return commissions.rates().stream().map(RateView::of).toList();
    }

    @Operation(operationId = "getCommissionStatement", summary = "Report each practitioner's share for a month")
    @GetMapping("/api/commissions")
    StatementView statement(@RequestParam int year, @RequestParam int month) {
        return StatementView.of(commissions.statementOf(CommissionPeriod.of(year, month)));
    }

    @Operation(operationId = "closeCommissionPeriod", summary = "Close a month, freezing what is owed")
    @PostMapping("/api/commissions/closing")
    StatementView close(@RequestParam int year, @RequestParam int month) {
        return StatementView.of(commissions.close(CommissionPeriod.of(year, month)));
    }
}
