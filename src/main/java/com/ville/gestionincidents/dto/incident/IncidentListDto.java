package com.ville.gestionincidents.dto.incident;

import com.ville.gestionincidents.enumeration.CategorieIncident;
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
    private CategorieIncident categorie;
    private StatutIncident statut;
    private String adresse;
    private LocalDateTime dateDeclaration;

    // Photo principale (si elle existe)
    private String photoPrincipale;

    // Nombre de photos
    private int nombrePhotos;
}