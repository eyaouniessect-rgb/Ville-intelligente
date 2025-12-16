package com.ville.gestionincidents.dto.incident;

import com.ville.gestionincidents.enumeration.StatutIncident;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO pour afficher un incident dans une liste
 * Contient uniquement les infos essentielles
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentListDto {

    private Long id;
    private String description;

    private StatutIncident statut;
    private LocalDateTime dateDeclaration;

    // ✅ NOUVEAUX CHAMPS (métier correct)
    private String departementNom;
    private String quartierNom;

    // Photo principale (si elle existe)
    private String photoPrincipale;

    // Nombre de photos
    private int nombrePhotos;
}
