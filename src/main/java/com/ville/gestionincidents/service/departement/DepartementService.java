package com.ville.gestionincidents.service.departement;

import com.ville.gestionincidents.entity.Departement;
import com.ville.gestionincidents.entity.ServiceMunicipal;
import java.util.List;

/**
 * Interface du service Département
 * Définit les méthodes disponibles
 */
public interface DepartementService {

    // ==================== GESTION DES DÉPARTEMENTS ====================

    Departement createDepartement(Departement departement);

    Departement updateDepartement(Long id, Departement departement);

    void deleteDepartement(Long id);

    Departement findById(Long id);

    List<Departement> findAll();

    // ==================== STATISTIQUES ====================

    long countDepartements();

}
