package com.ville.gestionincidents.service.quartier;

import com.ville.gestionincidents.entity.Quartier;
import org.springframework.stereotype.Service;

import java.util.List;

public interface QuartierService {

    // ==================== GESTION DES QUARTIERS ====================

    /**
     * Créer un nouveau quartier
     */
    Quartier createQuartier(Quartier quartier);

    /**
     * Mettre à jour un quartier existant
     */
    Quartier updateQuartier(Long id, Quartier quartier);

    /**
     * Supprimer un quartier
     */
    void deleteQuartier(Long id);

    /**
     * Trouver un quartier par son ID
     */
    Quartier findById(Long id);

    /**
     * Récupérer tous les quartiers
     */
    List<Quartier> findAll();

    /**
     * Trouver les quartiers par code postal
     */
    List<Quartier> findByCodePostal(String codePostal);

    // ==================== STATISTIQUES ====================

    /**
     * Compter le nombre total de quartiers
     */
    long countQuartiers();
}