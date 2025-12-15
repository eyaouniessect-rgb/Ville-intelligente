package com.ville.gestionincidents.service.serviceMunicipal;

import com.ville.gestionincidents.entity.Departement;
import com.ville.gestionincidents.entity.ServiceMunicipal;
import com.ville.gestionincidents.repository.DepartementRepository;
import com.ville.gestionincidents.repository.ServiceMunicipalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceMunicipalServiceImpl implements ServiceMunicipalService {

    private final ServiceMunicipalRepository serviceMunicipalRepository;
    private final DepartementRepository departementRepository;

    @Override
    @Transactional
    public ServiceMunicipal addServiceToDepartement(Long departementId, ServiceMunicipal service) {

        Departement departement = departementRepository.findById(departementId)
                .orElseThrow(() -> new RuntimeException("Département introuvable"));

        if (serviceMunicipalRepository
                .findByNomAndDepartement(service.getNom(), departement)
                .isPresent()) {
            throw new RuntimeException("Un service avec ce nom existe déjà dans ce département");
        }

        service.setDepartement(departement);
        return serviceMunicipalRepository.save(service);
    }

    @Override
    @Transactional
    public ServiceMunicipal updateService(Long serviceId, ServiceMunicipal service) {

        ServiceMunicipal existing = findServiceById(serviceId);

        if (!existing.getNom().equals(service.getNom()) &&
                serviceMunicipalRepository
                        .findByNomAndDepartement(service.getNom(), existing.getDepartement())
                        .isPresent()) {
            throw new RuntimeException("Un service avec ce nom existe déjà dans ce département");
        }

        existing.setNom(service.getNom());
        existing.setDescription(service.getDescription());

        return serviceMunicipalRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteService(Long serviceId) {

        ServiceMunicipal service = findServiceById(serviceId);

        if (!service.getIncidents().isEmpty()) {
            throw new RuntimeException(
                    "Impossible de supprimer ce service. Il contient "
                            + service.getIncidents().size() + " incident(s).");
        }

        serviceMunicipalRepository.delete(service);
    }

    @Override
    public ServiceMunicipal findServiceById(Long serviceId) {
        return serviceMunicipalRepository.findById(serviceId)
                .orElseThrow(() ->
                        new RuntimeException("Service introuvable avec l'ID : " + serviceId));
    }

    @Override
    public List<ServiceMunicipal> findServicesByDepartement(Long departementId) {
        Departement departement = departementRepository.findById(departementId)
                .orElseThrow(() -> new RuntimeException("Département introuvable"));
        return serviceMunicipalRepository.findByDepartement(departement);
    }

    @Override
    public List<ServiceMunicipal> findByDepartement(Departement departement) {
        return serviceMunicipalRepository.findByDepartement(departement);
    }

    @Override
    public List<ServiceMunicipal> findAllServices() {
        return serviceMunicipalRepository.findAll();
    }

    @Override
    public long countServices() {
        return serviceMunicipalRepository.count();
    }
}
