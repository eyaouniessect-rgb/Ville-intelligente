package com.ville.gestionincidents.service.dashboardAdmin;

import com.ville.gestionincidents.dto.dashboardAdmin.DelaiResolutionDto;
import com.ville.gestionincidents.dto.dashboardAdmin.IncidentParQuartierDto;
import com.ville.gestionincidents.dto.dashboardAdmin.IncidentParServiceDto;
import com.ville.gestionincidents.entity.Departement;
import com.ville.gestionincidents.entity.Rapport;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface DashboardService {

    // ==================== COMPTEURS ====================
    long countTotalIncidentsByDepartement(
            Departement departement,
            LocalDate dateDebut,
            LocalDate dateFin,
            Long serviceId,
            Long quartierId);

    long countIncidentsResolusByDepartement(
            Departement departement,
            LocalDate dateDebut,
            LocalDate dateFin,
            Long serviceId,
            Long quartierId);

    long countIncidentsEnCoursByDepartement(
            Departement departement,
            LocalDate dateDebut,
            LocalDate dateFin,
            Long serviceId,
            Long quartierId);

    double calculerDelaiMoyenResolutionByDepartement(
            Departement departement,
            LocalDate dateDebut,
            LocalDate dateFin,
            Long serviceId,
            Long quartierId);

    // ==================== DONNÉES POUR GRAPHIQUES ====================
    List<IncidentParServiceDto> getIncidentsParServiceByDepartement(
            Departement departement,
            LocalDate dateDebut,
            LocalDate dateFin,
            Long serviceId,
            Long quartierId);

    List<IncidentParQuartierDto> getIncidentsParQuartierByDepartement(
            Departement departement,
            LocalDate dateDebut,
            LocalDate dateFin,
            Long serviceId,
            Long quartierId);

    List<DelaiResolutionDto> getDelaiResolutionParServiceByDepartement(
            Departement departement,
            LocalDate dateDebut,
            LocalDate dateFin,
            Long serviceId,
            Long quartierId);

    Map<String, Long> getIncidentsParStatutByDepartement(
            Departement departement,
            LocalDate dateDebut,
            LocalDate dateFin,
            Long serviceId,
            Long quartierId);

    Map<String, Long> getIncidentsParMoisByDepartement(
            Departement departement,
            LocalDate dateDebut,
            LocalDate dateFin,
            Long serviceId,
            Long quartierId);

    // ==================== EXPORTS (Téléchargement direct) =====================
    void exportCsv(
            LocalDate dateDebut,
            LocalDate dateFin,
            Long serviceId,
            Long quartierId,
            Writer writer) throws Exception;

    void exportPdf(
            LocalDate dateDebut,
            LocalDate dateFin,
            Long serviceId,
            Long quartierId,
            OutputStream outputStream) throws Exception;

    // ==================== EXPORTS (Sauvegarde en BD) =====================
    /**
     * Génère un rapport CSV et le sauvegarde dans la base de données
     */
    Rapport exportCsvAndSave(
            LocalDate dateDebut,
            LocalDate dateFin,
            Long serviceId,
            Long quartierId) throws Exception;

    /**
     * Génère un rapport PDF et le sauvegarde dans la base de données
     */
    Rapport exportPdfAndSave(
            LocalDate dateDebut,
            LocalDate dateFin,
            Long serviceId,
            Long quartierId) throws Exception;

    // ==================== GESTION DES RAPPORTS =====================
    /**
     * Récupère tous les rapports du département de l'utilisateur connecté
     */
    List<Rapport> getRapportsByDepartement();

    /**
     * Récupère un rapport spécifique par son ID
     */
    Rapport getRapportById(Long rapportId);

    /**
     * Télécharge le fichier d'un rapport existant
     */
    byte[] telechargerRapport(Long rapportId) throws IOException;

    /**
     * Supprime un rapport (fichier + enregistrement BD)
     */
    void supprimerRapport(Long rapportId) throws IOException;

    // ==================== DÉLAI MOYEN GLOBAL =====================
    double calculDelaiMoyenGlobal();
}