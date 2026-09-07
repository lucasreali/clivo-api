package com.example.clivoapi.core.catalog;

import com.example.clivoapi.common.exception.BusinessException;
import com.example.clivoapi.common.exception.ResourceNotFoundException;
import com.example.clivoapi.core.catalog.internal.ServiceRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

@org.springframework.stereotype.Service
@Transactional
public class CatalogService {

    private final ServiceRepository services;

    CatalogService(ServiceRepository services) {
        this.services = services;
    }

    public ServiceSnapshot register(ServiceDetails details) {
        requireNameFree(details, null);
        return services.save(new Service(details)).snapshot();
    }

    public ServiceSnapshot describe(UUID id, ServiceDetails details) {
        Service service = serviceOf(id);
        requireNameFree(details, service.id());
        service.describeAs(details);
        return services.save(service).snapshot();
    }

    public ServiceSnapshot deactivate(UUID id) {
        Service service = serviceOf(id);
        service.deactivate();
        return services.save(service).snapshot();
    }

    @Transactional(readOnly = true)
    public List<ServiceSnapshot> findAll() {
        return services.findAllByOrderByNameAsc().stream().map(Service::snapshot).toList();
    }

    @Transactional(readOnly = true)
    public ServiceSnapshot findOne(UUID id) {
        return serviceOf(id).snapshot();
    }

    @Transactional(readOnly = true)
    public Service reference(UUID id) {
        return serviceOf(id);
    }

    private void requireNameFree(ServiceDetails details, UUID owner) {
        services.findByNameIgnoreCase(details.name())
                .filter(existing -> !existing.id().equals(owner))
                .ifPresent(existing -> refuseDuplicate(existing.name()));
    }

    private void refuseDuplicate(String name) {
        throw new BusinessException("service %s is already in the catalogue of this clinic".formatted(name));
    }

    private Service serviceOf(UUID id) {
        return services.findById(id).orElseThrow(() -> new ResourceNotFoundException("Service", id));
    }
}
