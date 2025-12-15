package com.ville.gestionincidents.service.incident;

import com.ville.gestionincidents.dto.incident.IncidentCreateDto;
import com.ville.gestionincidents.dto.incident.IncidentDetailsDto;
import com.ville.gestionincidents.dto.incident.IncidentListDto;
import com.ville.gestionincidents.enumeration.StatutIncident;

import java.util.List;

/**
 * Déclare un incident envoyé depuis le formulaire citoyen.
 */
public interface IncidentService {

    //declarer un incident cree par eya
    void creerIncident(IncidentCreateDto dto);
     //recuperer les incident d'un citoyen donnee creer par eya
    List<IncidentListDto> getIncidentsForCurrentUser();

    //recuperer les incidents par status d'un citoyen connecte
    List<IncidentListDto> getIncidentsByStatutForCurrentUser(StatutIncident statut);

    IncidentDetailsDto getIncidentDetailsForCurrentUser(Long id); // Vérification propriétaire

    //developper par mayssa
    int countForCurrentUser(); // Total incidents

    int countSignaleForCurrentUser();

    int countPrisEnChargeForCurrentUser();

    int countEnResolutionForCurrentUser();

    int countResoluForCurrentUser();

    int countClotureForCurrentUser();
}