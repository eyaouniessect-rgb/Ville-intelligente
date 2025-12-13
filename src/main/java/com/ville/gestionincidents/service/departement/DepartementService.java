package com.ville.gestionincidents.service.departement;

import com.ville.gestionincidents.entity.Departement;
import java.util.List;

/**
 * Interface du service Département
 * Définit les méthodes disponibles
 */
public interface DepartementService {

    /**
     * Retourne tous les départements
     * Utilisé pour remplir les listes déroulantes
     */
    List<Departement> findAll();
}
