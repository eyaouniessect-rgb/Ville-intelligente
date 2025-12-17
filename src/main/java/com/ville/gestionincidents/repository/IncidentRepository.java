package com.ville.gestionincidents.repository;

import com.ville.gestionincidents.entity.*;
import com.ville.gestionincidents.enumeration.StatutIncident;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {

    // ===================== BASIQUE =====================

    int countByCitoyenEmail(String email);
    int countByCitoyenEmailAndStatut(String email, StatutIncident statut);
    // ===================== QUARTIER =====================
    long countByQuartier(Quartier quartier);

    List<Incident> findByCitoyen(Utilisateur citoyen);
    List<Incident> findByCitoyenEmail(String email);
    List<Incident> findByCitoyenIdAndStatut(Long citoyenId, StatutIncident statut);




    // ===================== DATES =====================

    long countByDateDeclarationBetween(LocalDateTime debut, LocalDateTime fin);

    long countByDateDeclarationBetweenAndStatut(
            LocalDateTime debut,
            LocalDateTime fin,
            StatutIncident statut
    );

    long countByDateDeclarationBetweenAndStatutIn(
            LocalDateTime debut,
            LocalDateTime fin,
            List<StatutIncident> statuts
    );

    List<Incident> findByDateDeclarationBetween(LocalDateTime debut, LocalDateTime fin);

    List<Incident> findByDateDeclarationBetweenAndStatut(
            LocalDateTime debut,
            LocalDateTime fin,
            StatutIncident statut
    );

    // ===================== SERVICE =====================

    long countByServiceAndDateDeclarationBetween(
            ServiceMunicipal service,
            LocalDateTime debut,
            LocalDateTime fin
    );

    List<Incident> findByServiceAndDateDeclarationBetweenAndStatut(
            ServiceMunicipal service,
            LocalDateTime debut,
            LocalDateTime fin,
            StatutIncident statut
    );

    // ===================== SERVICE → DEPARTEMENT =====================

    // ✅ NOUVELLE MÉTHODE - Relation directe avec Departement
    long countByDepartementAndDateDeclarationBetween(
            Departement departement,
            LocalDateTime debut,
            LocalDateTime fin
    );

    long countByDepartementAndDateDeclarationBetweenAndStatut(
            Departement departement,
            LocalDateTime debut,
            LocalDateTime fin,
            StatutIncident statut
    );

    long countByDepartementAndDateDeclarationBetweenAndStatutIn(
            Departement departement,
            LocalDateTime debut,
            LocalDateTime fin,
            List<StatutIncident> statuts
    );

    long countByDepartementAndStatut(
            Departement departement,
            StatutIncident statut
    );

    // ✅ FILTRES DASHBOARD - Relation directe
    @Query("""
    SELECT i FROM Incident i
    WHERE i.departement = :departement
    AND (:serviceId IS NULL OR i.service.id = :serviceId)
    AND (:statut IS NULL OR i.statut = :statut)
    AND i.dateDeclaration BETWEEN :dateDebut AND :dateFin
""")
    Page<Incident> findByFilters(
            @Param("departement") Departement departement,
            @Param("serviceId") Long serviceId,
            @Param("statut") StatutIncident statut,
            @Param("dateDebut") LocalDateTime dateDebut,
            @Param("dateFin") LocalDateTime dateFin,
            Pageable pageable
    );

    // ✅ COUNT NON ASSIGNÉS
    @Query("""
    SELECT COUNT(i)
    FROM Incident i
    WHERE i.departement = :departement
    AND i.agent IS NULL
""")
    long countNonAssignesByDepartement(@Param("departement") Departement departement);



    // Tous les incidents résolus
    List<Incident> findByStatut(StatutIncident statut);

    // Incidents résolus par service
    List<Incident> findByServiceAndStatut(ServiceMunicipal service, StatutIncident statut);

    // Dans IncidentRepository.java

    // Compter les incidents assignés à un agent par statut
    long countByAgentAndStatut(Utilisateur agent, StatutIncident statut);

    // Récupérer tous les incidents d'un agent
    List<Incident> findByAgentOrderByDateDeclarationDesc(Utilisateur agent);

    // Récupérer les incidents d'un agent par statut
    List<Incident> findByAgentAndStatutOrderByDateDeclarationDesc(
            Utilisateur agent,
            StatutIncident statut
    );

    // Compter tous les incidents d'un agent
    long countByAgent(Utilisateur agent);

    // ✅ Incidents NON assignés d’un département précis
    List<Incident> findByServiceIsNullAndDepartement_Id(Long departementId);

    // ✅ CORRECT - Relation directe avec Departement
    @Query("""
    SELECT i FROM Incident i
    WHERE i.agent IS NULL
    AND i.departement = :departement
    ORDER BY i.dateDeclaration DESC
""")
    List<Incident> findIncidentsNonAssignesParDepartement(@Param("departement") Departement departement);
    // ✅ Méthode pour récupérer tous les incidents d'un département
    List<Incident> findByDepartementAndDateDeclarationBetween(
            Departement departement,
            LocalDateTime debut,
            LocalDateTime fin
    );

    // ✅ Méthode pour récupérer les incidents résolus d'un département
    List<Incident> findByDepartementAndDateDeclarationBetweenAndStatut(
            Departement departement,
            LocalDateTime debut,
            LocalDateTime fin,
            StatutIncident statut
    );

}
