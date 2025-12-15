package com.ville.gestionincidents.service.departement;

import com.ville.gestionincidents.entity.Departement;
import com.ville.gestionincidents.repository.DepartementRepository;
import com.ville.gestionincidents.repository.ServiceMunicipalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class DepartementServiceImpl implements DepartementService {

    private final DepartementRepository departementRepository;
    private final ServiceMunicipalRepository serviceMunicipalRepository;

    // ==================== GESTION DES DÉPARTEMENTS ====================

    @Override
    @Transactional
    public Departement createDepartement(Departement departement) {

        // Vérifier si le département existe déjà
        if (departementRepository.existsByNom(departement.getNom())) {
            throw new RuntimeException("Un département avec ce nom existe déjà");
        }

        Departement saved = departementRepository.save(departement);
        return saved;
    }

    @Override
    @Transactional
    public Departement updateDepartement(Long id, Departement departement) {
        Departement existing = findById(id);

        // Vérifier si le nouveau nom existe déjà (sauf si c'est le même département)
        if (!existing.getNom().equals(departement.getNom()) &&
                departementRepository.existsByNom(departement.getNom())) {
            throw new RuntimeException("Un département avec ce nom existe déjà");
        }

        existing.setNom(departement.getNom());
        existing.setDescription(departement.getDescription());
        existing.setEmail(departement.getEmail());
        existing.setTelephone(departement.getTelephone());

        Departement updated = departementRepository.save(existing);
        return updated;
    }

    @Override
    @Transactional
    public void deleteDepartement(Long id) {

        Departement departement = findById(id);

        // Vérifier s'il y a des services associés
        long serviceCount = serviceMunicipalRepository.countByDepartement(departement);
        if (serviceCount > 0) {
            throw new RuntimeException("Impossible de supprimer ce département. Il contient " + serviceCount + " service(s).");
        }

        departementRepository.deleteById(id);
    }



    @Override
    public Departement findById(Long id) {
        return departementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Département introuvable avec l'ID : " + id));
    }

    @Override
    public List<Departement> findAll() {
        return departementRepository.findAll();
    }



    // ==================== STATISTIQUES ====================

    @Override
    public long countDepartements() {
        return departementRepository.count();
    }


}