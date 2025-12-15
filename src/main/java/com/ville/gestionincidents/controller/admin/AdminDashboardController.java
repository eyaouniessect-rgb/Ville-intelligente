package com.ville.gestionincidents.controller.admin;

import com.ville.gestionincidents.service.dashboardAdmin.DashboardService;
import com.ville.gestionincidents.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final DashboardService dashboardService;
    private final CurrentUserService currentUserService;

    /**
     * Affiche le dashboard principal avec tous les graphiques
     */
    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(required = false) LocalDate dateDebut,
            @RequestParam(required = false) LocalDate dateFin,
            Model model) {

        // Si pas de dates, prendre les 30 derniers jours
        if (dateDebut == null) {
            dateDebut = LocalDate.now().minusDays(30);
        }
        if (dateFin == null) {
            dateFin = LocalDate.now();
        }

        // Récupérer l'admin connecté
        var admin = currentUserService.getCurrentUser();

        // Statistiques générales
        model.addAttribute("totalIncidents",
                dashboardService.countTotalIncidents(dateDebut, dateFin));
        model.addAttribute("incidentsResolus",
                dashboardService.countIncidentsResolus(dateDebut, dateFin));
        model.addAttribute("incidentsEnCours",
                dashboardService.countIncidentsEnCours(dateDebut, dateFin));
        model.addAttribute("delaiMoyenResolution",
                dashboardService.calculerDelaiMoyenResolution(dateDebut, dateFin));

        // Données pour les graphiques
        model.addAttribute("incidentsParService",
                dashboardService.getIncidentsParService(dateDebut, dateFin));
        model.addAttribute("incidentsParQuartier",
                dashboardService.getIncidentsParQuartier(dateDebut, dateFin));
        model.addAttribute("delaiResolutionParService",
                dashboardService.getDelaiResolutionParService(dateDebut, dateFin));
        model.addAttribute("incidentsParStatut",
                dashboardService.getIncidentsParStatut(dateDebut, dateFin));
        model.addAttribute("incidentsParMois",
                dashboardService.getIncidentsParMois(dateDebut, dateFin));

        // Filtres de date
        model.addAttribute("dateDebut", dateDebut);
        model.addAttribute("dateFin", dateFin);
        model.addAttribute("admin", admin);

        return "admin/dashboard";
    }

    /**
     * Export CSV des statistiques
     */
    @GetMapping("/dashboard/export/csv")
    public void exportCsv(
            @RequestParam(required = false) String dateDebut,
            @RequestParam(required = false) String dateFin,
            HttpServletResponse response) {

        try {
            // Conversion et validation des dates
            LocalDate dateDebutLocal = null;
            LocalDate dateFinLocal = null;
            
            if (dateDebut != null && !dateDebut.isEmpty()) {
                try {
                    dateDebutLocal = LocalDate.parse(dateDebut);
                } catch (Exception e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.setContentType("text/plain; charset=UTF-8");
                    response.getWriter().write("Erreur : Format de date de début invalide");
                    response.getWriter().flush();
                    return;
                }
            }
            
            if (dateFin != null && !dateFin.isEmpty()) {
                try {
                    dateFinLocal = LocalDate.parse(dateFin);
                } catch (Exception e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.setContentType("text/plain; charset=UTF-8");
                    response.getWriter().write("Erreur : Format de date de fin invalide");
                    response.getWriter().flush();
                    return;
                }
            }
            
            // Valeurs par défaut si non fournies
            if (dateDebutLocal == null) dateDebutLocal = LocalDate.now().minusDays(30);
            if (dateFinLocal == null) dateFinLocal = LocalDate.now();

            if (dateDebutLocal.isAfter(dateFinLocal)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setContentType("text/plain; charset=UTF-8");
                response.getWriter().write("Erreur : La date de début doit être antérieure à la date de fin");
                response.getWriter().flush();
                return;
            }

            // Formatage du nom de fichier
            String fileName = "rapport_incidents_" +
                    dateDebutLocal.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "_" +
                    dateFinLocal.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv";

            // Encodage UTF-8 avec BOM pour Excel
            response.setContentType("text/csv; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"" + fileName + "\"; filename*=UTF-8''" +
                            URLEncoder.encode(fileName, StandardCharsets.UTF_8));

            // Écrire le BOM UTF-8 pour Excel
            response.getWriter().write('\ufeff');

            // Export des données
            dashboardService.exportCsv(dateDebutLocal, dateFinLocal, response.getWriter());

            // Flush pour s'assurer que tout est écrit
            response.getWriter().flush();

        } catch (Exception e) {
            try {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.setContentType("text/plain; charset=UTF-8");
                response.getWriter().write("Erreur lors de l'export CSV : " + e.getMessage());
                response.getWriter().flush();
            } catch (IOException ioException) {
                // Log l'erreur
                System.err.println("Erreur lors de l'écriture de l'erreur : " + ioException.getMessage());
            }
        }
    }

    /**
     * Export PDF des statistiques
     */
    @GetMapping("/dashboard/export/pdf")
    public void exportPdf(
            @RequestParam(required = false) String dateDebut,
            @RequestParam(required = false) String dateFin,
            HttpServletResponse response) {

        try {
            // Conversion et validation des dates
            LocalDate dateDebutLocal = null;
            LocalDate dateFinLocal = null;
            
            if (dateDebut != null && !dateDebut.isEmpty()) {
                try {
                    dateDebutLocal = LocalDate.parse(dateDebut);
                } catch (Exception e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.setContentType("text/plain; charset=UTF-8");
                    response.getWriter().write("Erreur : Format de date de début invalide");
                    response.getWriter().flush();
                    return;
                }
            }
            
            if (dateFin != null && !dateFin.isEmpty()) {
                try {
                    dateFinLocal = LocalDate.parse(dateFin);
                } catch (Exception e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.setContentType("text/plain; charset=UTF-8");
                    response.getWriter().write("Erreur : Format de date de fin invalide");
                    response.getWriter().flush();
                    return;
                }
            }
            
            // Valeurs par défaut si non fournies
            if (dateDebutLocal == null) dateDebutLocal = LocalDate.now().minusDays(30);
            if (dateFinLocal == null) dateFinLocal = LocalDate.now();

            if (dateDebutLocal.isAfter(dateFinLocal)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setContentType("text/plain; charset=UTF-8");
                response.getWriter().write("Erreur : La date de début doit être antérieure à la date de fin");
                response.getWriter().flush();
                return;
            }

            // Formatage du nom de fichier
            String fileName = "rapport_incidents_" +
                    dateDebutLocal.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "_" +
                    dateFinLocal.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf";

            // Configuration de la réponse PDF
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"" + fileName + "\"; filename*=UTF-8''" +
                            URLEncoder.encode(fileName, StandardCharsets.UTF_8));

            // Export des données
            dashboardService.exportPdf(dateDebutLocal, dateFinLocal, response.getOutputStream());

            // Flush pour s'assurer que tout est écrit
            response.getOutputStream().flush();

        } catch (Exception e) {
            try {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.setContentType("text/plain; charset=UTF-8");
                response.getWriter().write("Erreur lors de l'export PDF : " + e.getMessage());
                response.getWriter().flush();
            } catch (IOException ioException) {
                // Log l'erreur
                System.err.println("Erreur lors de l'écriture de l'erreur : " + ioException.getMessage());
            }
        }
    }
}