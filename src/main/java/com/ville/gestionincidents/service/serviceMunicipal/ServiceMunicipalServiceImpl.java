package com.ville.gestionincidents.service.serviceMunicipal;

import com.ville.gestionincidents.entity.ServiceMunicipal;
import com.ville.gestionincidents.entity.Departement;
import com.ville.gestionincidents.repository.DepartementRepository;
import com.ville.gestionincidents.repository.ServiceMunicipalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceMunicipalServiceImpl implements ServiceMunicipalService{
        private final ServiceMunicipalRepository serviceMunicipalRepository;
        private final DepartementRepository departementRepository;

        @Override
        @Transactional
        public ServiceMunicipal addServiceToDepartement(Long departementId, ServiceMunicipal service) {

            Departement departement = departementRepository.findById(departementId)
                    .orElseThrow(() -> new RuntimeException("Département introuvable"));

            // Vérifier si le service existe déjà dans ce département
            if (serviceMunicipalRepository.findByNomAndDepartement(service.getNom(), departement).isPresent()) {
                throw new RuntimeException("Un service avec ce nom existe déjà dans ce département");
            }

            service.setDepartement(departement);
            ServiceMunicipal saved = serviceMunicipalRepository.save(service);
            return saved;
        }

        @Override
        @Transactional
        public ServiceMunicipal updateService(Long serviceId, ServiceMunicipal service) {

            ServiceMunicipal existing = findServiceById(serviceId);

            // Vérifier si le nouveau nom existe déjà dans le département
            if (!existing.getNom().equals(service.getNom()) &&
                    serviceMunicipalRepository.findByNomAndDepartement(service.getNom(), existing.getDepartement()).isPresent()) {
                throw new RuntimeException("Un service avec ce nom existe déjà dans ce département");
            }

            existing.setNom(service.getNom());
            existing.setDescription(service.getDescription());

            ServiceMunicipal updated = serviceMunicipalRepository.save(existing);
            return updated;
        }

        @Override
        @Transactional
        public void deleteService(Long serviceId) {

            ServiceMunicipal service = findServiceById(serviceId);

            // Vérifier s'il y a des incidents associés
            if (!service.getIncidents().isEmpty()) {
                throw new RuntimeException("Impossible de supprimer ce service. Il contient " + service.getIncidents().size() + " incident(s).");
            }

            serviceMunicipalRepository.deleteById(serviceId);
        }

        @Override
        public ServiceMunicipal findServiceById(Long serviceId) {
            return serviceMunicipalRepository.findById(serviceId)
                    .orElseThrow(() -> new RuntimeException("Service introuvable avec l'ID : " + serviceId));
        }

        @Override
        public List<ServiceMunicipal> findServicesByDepartement(Long departementId) {
            Departement departement = departementRepository.findById(departementId)
                    .orElseThrow(() -> new RuntimeException("Département introuvable"));
            return serviceMunicipalRepository.findByDepartement(departement);
        }

        @Override
        public List<ServiceMunicipal> findAllServices() {
            return serviceMunicipalRepository.findAll();
        }

        // ==================== STATISTIQUES ====================



        @Override
        public long countServices() {
            return serviceMunicipalRepository.count();
        }

}
