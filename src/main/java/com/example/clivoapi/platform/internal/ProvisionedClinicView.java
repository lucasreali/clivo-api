package com.example.clivoapi.platform.internal;

import com.example.clivoapi.platform.ProvisionedClinic;

record ProvisionedClinicView(ClinicView clinic, ClinicUserView manager) {

    static ProvisionedClinicView of(ProvisionedClinic provisioned) {
        return new ProvisionedClinicView(
                ClinicView.of(provisioned.clinic()), ClinicUserView.of(provisioned.manager()));
    }
}
