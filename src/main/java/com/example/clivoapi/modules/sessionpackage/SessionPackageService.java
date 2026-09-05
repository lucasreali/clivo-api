package com.example.clivoapi.modules.sessionpackage;

import com.example.clivoapi.common.exception.ResourceNotFoundException;
import com.example.clivoapi.common.extension.CompletedEncounter;
import com.example.clivoapi.core.billing.BillingService;
import com.example.clivoapi.core.catalog.CatalogService;
import com.example.clivoapi.core.customer.CustomerService;
import com.example.clivoapi.modules.sessionpackage.internal.SessionPackageRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SessionPackageService {

    private final SessionPackageRepository packages;
    private final PackageParties parties;
    private final BillingService billing;

    SessionPackageService(
            SessionPackageRepository packages,
            CustomerService customers,
            CatalogService catalogue,
            BillingService billing) {
        this.packages = packages;
        this.parties = new PackageParties(customers, catalogue);
        this.billing = billing;
    }

    public SessionPackageSnapshot sell(PackagePurchase purchase) {
        return packages.save(parties.assemble(purchase)).snapshot();
    }

    public SessionPackageSnapshot cancel(Long id) {
        SessionPackage sold = packageOf(id);
        sold.cancel();
        return packages.save(sold).snapshot();
    }

    public Optional<SessionPackageSnapshot> consumeFor(CompletedEncounter encounter) {
        return activeFor(encounter).map(sold -> consume(sold, encounter.encounterId()));
    }

    @Transactional(readOnly = true)
    public List<SessionPackageSnapshot> of(Long customerId) {
        return packages.findByCustomerIdOrderByExpiresOnAsc(customerId).stream()
                .map(SessionPackage::snapshot)
                .toList();
    }

    @Transactional(readOnly = true)
    public SessionPackageSnapshot findOne(Long id) {
        return packageOf(id).snapshot();
    }

    private SessionPackageSnapshot consume(SessionPackage sold, Long encounterId) {
        sold.consumeSession(encounterId);
        billing.coverByPackage(encounterId);
        return packages.save(sold).snapshot();
    }

    private Optional<SessionPackage> activeFor(CompletedEncounter encounter) {
        return packages
                .findByCustomerIdAndServiceIdAndStatusOrderByExpiresOnAsc(
                        encounter.customerId(), encounter.serviceId(), SessionPackageStatus.ACTIVE)
                .stream()
                .filter(SessionPackage::isActive)
                .findFirst();
    }

    private SessionPackage packageOf(Long id) {
        return packages.findById(id).orElseThrow(() -> new ResourceNotFoundException("SessionPackage", id));
    }
}
