package com.ville.gestionincidents.dto.incident;

import com.ville.gestionincidents.enumeration.CategorieIncident;
import com.ville.gestionincidents.enumeration.StatutIncident;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO détaillé pour l'affichage d'un incident (page détail).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentDetailsDto {

    private Long id;
    private String description;
    private CategorieIncident categorie;
    private StatutIncident statut;
    private String adresse;
    private Double latitude;
    private Double longitude;
    private LocalDateTime dateDeclaration;
    private LocalDateTime dateDerniereMiseAJour;
    private LocalDateTime dateResolutionEstimee;

    private List<PhotoDto> photos;
}
