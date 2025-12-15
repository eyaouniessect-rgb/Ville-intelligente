package com.ville.gestionincidents.service.dashboardAdmin;

import com.ville.gestionincidents.dto.dashboardAdmin.DelaiResolutionDto;
import com.ville.gestionincidents.dto.dashboardAdmin.IncidentParQuartierDto;
import com.ville.gestionincidents.dto.dashboardAdmin.IncidentParServiceDto;

import java.io.OutputStream;
import java.io.Writer;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface DashboardService {

    // ==================== COMPTEURS ====================
    long countTotalIncidents(LocalDate dateDebut, LocalDate dateFin);
    long countIncidentsResolus(LocalDate dateDebut, LocalDate dateFin);
    long countIncidentsEnCours(LocalDate dateDebut, LocalDate dateFin);
    double calculerDelaiMoyenResolution(LocalDate dateDebut, LocalDate dateFin);

    // ==================== DONNÉES POUR GRAPHIQUES ====================
    List<IncidentParServiceDto> getIncidentsParService(LocalDate dateDebut, LocalDate dateFin);
    List<IncidentParQuartierDto> getIncidentsParQuartier(LocalDate dateDebut, LocalDate dateFin);
    List<DelaiResolutionDto> getDelaiResolutionParService(LocalDate dateDebut, LocalDate dateFin);
    Map<String, Long> getIncidentsParStatut(LocalDate dateDebut, LocalDate dateFin);
    Map<String, Long> getIncidentsParMois(LocalDate dateDebut, LocalDate dateFin);

    // ==================== EXPORTS ====================
    void exportCsv(LocalDate dateDebut, LocalDate dateFin, Writer writer) throws Exception;
    void exportPdf(LocalDate dateDebut, LocalDate dateFin, OutputStream outputStream) throws Exception;
}