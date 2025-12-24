package com.ville.gestionincidents.dto.incident;

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

    private StatutIncident statut;

    private Double latitude;
    private Double longitude;

    private LocalDateTime dateDeclaration;
    private LocalDateTime dateDerniereMiseAJour;
    private LocalDateTime dateResolutionEstimee;


    private String departementNom;
    private String quartierNom;

    private List<PhotoDto> photos;
}
