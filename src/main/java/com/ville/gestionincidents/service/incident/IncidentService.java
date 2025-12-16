package com.ville.gestionincidents.service.incident;

import com.ville.gestionincidents.dto.incident.IncidentCreateDto;

import com.ville.gestionincidents.entity.Departement;
import com.ville.gestionincidents.entity.Incident;
import com.ville.gestionincidents.entity.Utilisateur;
import com.ville.gestionincidents.enumeration.PrioriteIncident;
import com.ville.gestionincidents.enumeration.StatutIncident;
import org.springframework.data.domain.Page;

import com.ville.gestionincidents.dto.incident.IncidentDetailsDto;
import com.ville.gestionincidents.dto.incident.IncidentListDto;


import java.time.LocalDate;
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


    Incident findByIdAndCheckOwner(Long id, String email); // Vérification propriétaire


    long countByDepartement(Departement departement);

    long countByDepartementAndStatut(Departement departement, StatutIncident statut);

    long countByDepartementAndStatutsEnCours(Departement departement);

    long countByDepartementAndServiceIsNull(Departement departement);

    long countNonAssignesByDepartement(Departement departement);


    Page<Incident> findByDepartementWithFilters(
            Departement departement,
            Long serviceId,
            String statut,
            LocalDate dateDebut,
            LocalDate dateFin,
            int page,
            int size
    );

    void assignerIncident(Long incidentId, Long serviceId, Long agentId, String commentaire, PrioriteIncident priorite);

    int countClotureForCurrentUser();

    // Dans IncidentService.java

    // Statistiques agent
    long countByAgent(Utilisateur agent);
    long countByAgentAndStatut(Utilisateur agent, StatutIncident statut);

    // Liste des incidents
    List<Incident> findByAgent(Utilisateur agent);
    List<Incident> findByAgentAndStatut(Utilisateur agent, StatutIncident statut);

    // Changer le statut d'un incident
    void changerStatut(Long incidentId, StatutIncident nouveauStatut, String commentaire);
//
    Incident findById(Long id);

}