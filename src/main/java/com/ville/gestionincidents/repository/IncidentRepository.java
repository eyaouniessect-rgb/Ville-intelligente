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

    long countByService_DepartementAndDateDeclarationBetween(
            Departement departement,
            LocalDateTime debut,
            LocalDateTime fin
    );

    long countByService_DepartementAndDateDeclarationBetweenAndStatut(
            Departement departement,
            LocalDateTime debut,
            LocalDateTime fin,
            StatutIncident statut
    );

    long countByService_DepartementAndDateDeclarationBetweenAndStatutIn(
            Departement departement,
            LocalDateTime debut,
            LocalDateTime fin,
            List<StatutIncident> statuts
    );

    List<Incident> findByAgentIsNull();

    // ===================== FILTRES DASHBOARD =====================

    @Query("""
        SELECT i FROM Incident i
        WHERE i.service.departement = :departement
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

    /// /////////count de nbre d'incident non asigne dun département
    @Query("""
    SELECT COUNT(i)
    FROM Incident i
    WHERE i.service.departement = :departement
      AND i.agent IS NULL
""")
    long countNonAssignesByDepartement(@Param("departement") Departement departement);

}
