package com.ville.gestionincidents.repository;

import com.ville.gestionincidents.entity.Incident;
import com.ville.gestionincidents.entity.Utilisateur;
import com.ville.gestionincidents.entity.Quartier;
import com.ville.gestionincidents.enumeration.StatutIncident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import com.ville.gestionincidents.entity.ServiceMunicipal;

import java.util.List;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {

    // Compter tous les incidents d'un citoyen
    int countByCitoyenEmail(String email);

    // Compter selon un statut précis
    int countByCitoyenEmailAndStatut(String email, StatutIncident statut);


    //pour recuperer les incidents d'un citoyen connecte
    List<Incident> findByCitoyen(Utilisateur citoyen);

    // Récupérer tous les incidents d'un citoyen A suprrimer rediger par mayssa
    List<Incident> findByCitoyenEmail(String email);


    //recuperre les incidents par statut qui correspond a un citoyen donne
    List<Incident> findByCitoyenIdAndStatut(Long citoyenId, StatutIncident statut);

    long countByQuartier(com.ville.gestionincidents.entity.Quartier quartier);

    // Compter les incidents entre deux dates
    long countByDateDeclarationBetween(LocalDateTime debut, LocalDateTime fin);

    // Compter les incidents par statut entre deux dates
    long countByDateDeclarationBetweenAndStatut(
            LocalDateTime debut, LocalDateTime fin, StatutIncident statut);

    // Compter les incidents par statuts multiples
    long countByDateDeclarationBetweenAndStatutIn(
            LocalDateTime debut, LocalDateTime fin, List<StatutIncident> statuts);

    // Trouver les incidents résolus entre deux dates
    List<Incident> findByDateDeclarationBetweenAndStatut(
            LocalDateTime debut, LocalDateTime fin, StatutIncident statut);

    // Compter les incidents par service entre deux dates
    long countByServiceAndDateDeclarationBetween(
            ServiceMunicipal service, LocalDateTime debut, LocalDateTime fin);

    // Compter les incidents par quartier entre deux dates
    long countByQuartierAndDateDeclarationBetween(
            Quartier quartier, LocalDateTime debut, LocalDateTime fin);

    // Trouver les incidents par service et statut entre deux dates
    List<Incident> findByServiceAndDateDeclarationBetweenAndStatut(
            ServiceMunicipal service, LocalDateTime debut, LocalDateTime fin, StatutIncident statut);

    // Trouver tous les incidents entre deux dates
    List<Incident> findByDateDeclarationBetween(LocalDateTime debut, LocalDateTime fin);

}