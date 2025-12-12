package com.ville.gestionincidents.service.incident;

import com.ville.gestionincidents.dto.incident.IncidentCreateDto;
import com.ville.gestionincidents.entity.Incident;

import java.util.List;

/**
 * Déclare un incident envoyé depuis le formulaire citoyen.
 */
public interface IncidentService {

    void creerIncident(IncidentCreateDto dto);

    //developper par mayssa
    int countByEmail(String email); // Total incidents

    int countInProgress(String email); // Nombre incidents en cours

    int countResolved(String email); // Nombre incidents résolus

    List<Incident> findByCitoyenEmail(String email); // Liste incidents

    Incident findByIdAndCheckOwner(Long id, String email); // Vérification propriétaire
}