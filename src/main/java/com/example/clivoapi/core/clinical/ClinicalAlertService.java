package com.example.clivoapi.core.clinical;

import com.example.clivoapi.common.exception.ResourceNotFoundException;
import com.example.clivoapi.core.access.AccessService;
import com.example.clivoapi.core.access.Role;
import com.example.clivoapi.core.access.RoleAccess;
import com.example.clivoapi.core.clinical.internal.ClinicalAlertRepository;
import com.example.clivoapi.core.customer.CustomerService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ClinicalAlertService {

    private final ClinicalAlertRepository alerts;
    private final CustomerService customers;
    private final AccessService access;
    private final RoleAccess roleAccess;

    ClinicalAlertService(
            ClinicalAlertRepository alerts,
            CustomerService customers,
            AccessService access,
            RoleAccess roleAccess) {
        this.alerts = alerts;
        this.customers = customers;
        this.access = access;
        this.roleAccess = roleAccess;
    }

    public ClinicalAlertSnapshot record(UUID customerId, AlertNote note, Role viewer) {
        roleAccess.requireClinicalRecord(viewer);
        ClinicalAlert alert = new ClinicalAlert(customers.reference(customerId), note, access.signedIn());
        return alerts.save(alert).snapshot();
    }

    public ClinicalAlertSnapshot rewrite(UUID id, AlertNote note, Role viewer) {
        roleAccess.requireClinicalRecord(viewer);
        ClinicalAlert alert = alertOf(id);
        alert.rewriteAs(note, access.signedIn());
        return alerts.save(alert).snapshot();
    }

    public void withdraw(UUID id, Role viewer) {
        roleAccess.requireClinicalRecord(viewer);
        alerts.delete(alertOf(id));
    }

    @Transactional(readOnly = true)
    public List<ClinicalAlertSnapshot> of(UUID customerId) {
        return alerts.findByCustomerIdOrderByRecordedAtDesc(customerId).stream()
                .map(ClinicalAlert::snapshot)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ClinicalAlertSnapshot> visibleTo(UUID customerId, Role viewer) {
        roleAccess.requireClinicalRecord(viewer);
        return of(customerId);
    }

    private ClinicalAlert alertOf(UUID id) {
        return alerts.findById(id).orElseThrow(() -> new ResourceNotFoundException("ClinicalAlert", id));
    }
}
