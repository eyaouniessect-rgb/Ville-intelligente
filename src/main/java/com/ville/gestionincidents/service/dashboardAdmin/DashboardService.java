package com.ville.gestionincidents.service.dashboardAdmin;

import com.ville.gestionincidents.dto.dashboardAdmin.DelaiResolutionDto;
import com.ville.gestionincidents.dto.dashboardAdmin.IncidentParQuartierDto;
import com.ville.gestionincidents.dto.dashboardAdmin.IncidentParServiceDto;
import com.ville.gestionincidents.entity.Departement;

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

    // ==================== EXPORTS =====================
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

    // ==================== DÉLAI MOYEN GLOBAL =====================
    double calculDelaiMoyenGlobal();
}