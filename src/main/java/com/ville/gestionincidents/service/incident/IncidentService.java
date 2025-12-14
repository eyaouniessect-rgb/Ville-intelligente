package com.ville.gestionincidents.service.incident;

import com.ville.gestionincidents.dto.incident.IncidentCreateDto;
import com.ville.gestionincidents.entity.Incident;

import java.util.List;

/**
 * Déclare un incident envoyé depuis le formulaire citoyen.
 */
public interface IncidentService {

    //declarer un incident cree par eya
    void creerIncident(IncidentCreateDto dto);
     //recuperer les incident d'un citoyen donnee creer par eya
    List<Incident> getIncidentsForCurrentUser();

    //recuperer les incidents par status d'un citoyen connecte
    List<Incident> getIncidentsByStatutForUser(String email, String statut);



    //developper par mayssa
    int countByEmail(String email); // Total incidents

    int countSignale(String email);

    int countPrisEnCharge(String email);

    int countEnResolution(String email);

    int countResolu(String email);

    int countCloture(String email);


    List<Incident> findByCitoyenEmail(String email); // Liste incidents

    Incident findByIdAndCheckOwner(Long id, String email); // Vérification propriétaire
}