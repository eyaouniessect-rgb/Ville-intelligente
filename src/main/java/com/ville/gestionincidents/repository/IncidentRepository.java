package com.ville.gestionincidents.repository;

import com.ville.gestionincidents.entity.Incident;
import com.ville.gestionincidents.entity.Utilisateur;
import com.ville.gestionincidents.enumeration.StatutIncident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}