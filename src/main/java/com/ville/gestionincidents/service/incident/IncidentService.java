package com.ville.gestionincidents.service.incident;

import com.ville.gestionincidents.dto.incident.IncidentCreateDto;

/**
 * Déclare un incident envoyé depuis le formulaire citoyen.
 */
public interface IncidentService {

    void creerIncident(IncidentCreateDto dto);
}