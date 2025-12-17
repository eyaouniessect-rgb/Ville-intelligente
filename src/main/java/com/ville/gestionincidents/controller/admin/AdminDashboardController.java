package com.ville.gestionincidents.controller.admin;

import com.ville.gestionincidents.dto.dashboardAdmin.DelaiResolutionDto;
import com.ville.gestionincidents.entity.Incident;
import com.ville.gestionincidents.entity.Rapport;
import com.ville.gestionincidents.entity.ServiceMunicipal;
import com.ville.gestionincidents.entity.Utilisateur;
import com.ville.gestionincidents.enumeration.PrioriteIncident;
import com.ville.gestionincidents.repository.UtilisateurRepository;
import com.ville.gestionincidents.security.CustomUserDetails;
import com.ville.gestionincidents.service.dashboardAdmin.DashboardService;
import com.ville.gestionincidents.security.CurrentUserService;
import com.ville.gestionincidents.service.incident.IncidentService;
import com.ville.gestionincidents.service.serviceMunicipal.ServiceMunicipalService;
import com.ville.gestionincidents.enumeration.StatutIncident;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.ville.gestionincidents.service.utilisateur.UtilisateurService;
import com.ville.gestionincidents.enumeration.Role;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.ville.gestionincidents.dto.utilisateur.agent.AgentDto;
import com.ville.gestionincidents.repository.IncidentRepository;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.ville.gestionincidents.enumeration.PrioriteIncident;
import com.ville.gestionincidents.repository.ServiceMunicipalRepository;

import com.ville.gestionincidents.service.notification.PreferenceNotificationService;
import  com.ville.gestionincidents.repository.QuartierRepository;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final DashboardService dashboardService;
    private final CurrentUserService currentUserService;
    private final IncidentService incidentService;
    private final ServiceMunicipalService serviceMunicipalService;
    private final UtilisateurService utilisateurService;
    private final UtilisateurRepository utilisateurRepository;
    private final IncidentRepository incidentRepository;
    @Autowired
    private ServiceMunicipalRepository serviceMunicipalRepository;
    private final QuartierRepository quartierRepository;

    /**
     * PAGE 1 : Dashboard principal avec la liste des incidents
     */
    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(required = false) Long serviceId,
            @RequestParam(required = false) String statut,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dateSignalement,

            @RequestParam(defaultValue = "0") int page,
            Model model) {

        var admin = currentUserService.getCurrentUser();
        var departement = admin.getDepartement();

        // ✅ Initialiser les dates pour le délai moyen
        LocalDate dateDebut = LocalDate.now().minusDays(30);
        LocalDate dateFin = LocalDate.now();

        model.addAttribute("admin", admin);

        // ✅ Délai moyen global (sans filtrage par date)
        double delaiMoyenGlobal = dashboardService.calculDelaiMoyenGlobal();
        model.addAttribute("delaiMoyenResolution", delaiMoyenGlobal);

        // ✅ CORRECTION ICI : Ajouter serviceId et quartierId (null car pas de filtre sur le dashboard principal)
        model.addAttribute(
                "delaiResolutionParService",
                dashboardService.getDelaiResolutionParServiceByDepartement(
                        departement, dateDebut, dateFin, null, null) // ✅ 5 paramètres
        );

        // Statistiques
        long totalIncidents = incidentService.countByDepartement(departement);
        model.addAttribute("totalIncidents", totalIncidents);

        model.addAttribute("incidentsSignales",
                incidentService.countByDepartementAndStatut(departement, StatutIncident.SIGNALE));
        model.addAttribute("incidentsPrisEnCharge",
                incidentService.countByDepartementAndStatut(departement, StatutIncident.PRIS_EN_CHARGE));
        model.addAttribute("incidentsEnResolution",
                incidentService.countByDepartementAndStatut(departement, StatutIncident.EN_RESOLUTION));
        model.addAttribute("incidentsResolus",
                incidentService.countByDepartementAndStatut(departement, StatutIncident.RESOLU));
        model.addAttribute("incidentsClotures",
                incidentService.countByDepartementAndStatut(departement, StatutIncident.CLOTURE));
        model.addAttribute("incidentsEnCours",
                incidentService.countByDepartementAndStatutsEnCours(departement));
        model.addAttribute("totalAgents",
                utilisateurService.countAgentsByDepartement(departement));
        model.addAttribute("incidentsNonAssignes",
                incidentService.countNonAssignesByDepartement(departement));

        model.addAttribute("services",
                serviceMunicipalService.findByDepartement(departement));
        model.addAttribute("statuts", StatutIncident.values());

        // Liste des incidents avec filtres
        var incidents = incidentService.findByDepartementWithFilters(
                departement, serviceId, statut, dateSignalement, dateSignalement, page, 20);

        model.addAttribute("incidents", incidents.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", incidents.getTotalPages());

        // Garder les filtres
        model.addAttribute("serviceId", serviceId);
        model.addAttribute("statut", statut);
        model.addAttribute("dateSignalement", dateSignalement);
        model.addAttribute("priorites", PrioriteIncident.values());

        return "admin/dashboard";
    }
    /**
     * PAGE 2 : Rapports analytiques avec graphiques Chart.js
     */
    @GetMapping("/dashboard/rapport")
    public String rapportAnalytique(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dateDebut,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dateFin,

            @RequestParam(required = false) Long serviceId,
            @RequestParam(required = false) Long quartierId,
            Model model) {

        // Si pas de dates, prendre les 30 derniers jours
        if (dateDebut == null) {
            dateDebut = LocalDate.now().minusDays(30);
        }
        if (dateFin == null) {
            dateFin = LocalDate.now();
        }

        // Récupérer l'admin et son département
        var admin = currentUserService.getCurrentUser();
        var departement = admin.getDepartement();

        // ========== STATISTIQUES ==========
        model.addAttribute("totalIncidents",
                dashboardService.countTotalIncidentsByDepartement(
                        departement, dateDebut, dateFin, serviceId, quartierId));

        model.addAttribute("incidentsResolus",
                dashboardService.countIncidentsResolusByDepartement(
                        departement, dateDebut, dateFin, serviceId, quartierId));

        model.addAttribute("incidentsEnCours",
                dashboardService.countIncidentsEnCoursByDepartement(
                        departement, dateDebut, dateFin, serviceId, quartierId));

        model.addAttribute("delaiMoyenResolution",
                dashboardService.calculerDelaiMoyenResolutionByDepartement(
                        departement, dateDebut, dateFin, serviceId, quartierId));

        // ========== GRAPHIQUES ==========
        model.addAttribute("incidentsParService",
                dashboardService.getIncidentsParServiceByDepartement(
                        departement, dateDebut, dateFin, serviceId, quartierId));

        model.addAttribute("incidentsParQuartier",
                dashboardService.getIncidentsParQuartierByDepartement(
                        departement, dateDebut, dateFin, serviceId, quartierId));

        model.addAttribute("delaiResolutionParService",
                dashboardService.getDelaiResolutionParServiceByDepartement(
                        departement, dateDebut, dateFin, serviceId, quartierId));

        model.addAttribute("incidentsParStatut",
                dashboardService.getIncidentsParStatutByDepartement(
                        departement, dateDebut, dateFin, serviceId, quartierId));

        model.addAttribute("incidentsParMois",
                dashboardService.getIncidentsParMoisByDepartement(
                        departement, dateDebut, dateFin, serviceId, quartierId));

        // ========== FILTRES ==========
        model.addAttribute("dateDebut", dateDebut);
        model.addAttribute("dateFin", dateFin);
        model.addAttribute("serviceId", serviceId);
        model.addAttribute("quartierId", quartierId);

        // ========== LISTES POUR LES SELECTS ==========
        model.addAttribute("services",
                serviceMunicipalRepository.findByDepartement(departement));
        model.addAttribute("quartiers",
                quartierRepository.findAll()); // Tous les quartiers

        return "admin/rapport_analytique";
    }

    /**
     * Export CSV des statistiques
     */
    @GetMapping("/dashboard/export/csv")
    public void exportCsv(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            @RequestParam(required = false) Long serviceId,
            @RequestParam(required = false) Long quartierId,
            HttpServletResponse response) throws IOException {

        if (dateDebut == null) dateDebut = LocalDate.now().minusDays(30);
        if (dateFin == null) dateFin = LocalDate.now();

        try {
            // ✅ 1) Générer + sauvegarder
            Rapport rapport = dashboardService.exportCsvAndSave(
                    dateDebut, dateFin, serviceId, quartierId
            );

            // ✅ 2) Télécharger le fichier sauvegardé
            Path path = Paths.get(rapport.getCheminFichier());

            response.setContentType("text/csv");
            response.setCharacterEncoding("UTF-8");
            response.setHeader(
                    "Content-Disposition",
                    "attachment; filename=" + rapport.getNomFichier()
            );

            Files.copy(path, response.getOutputStream());
            response.flushBuffer();

        } catch (Exception e) {
            response.reset();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Erreur export CSV : " + e.getMessage());
        }
    }


    /**
     * Export PDF des statistiques
     */
    @GetMapping("/dashboard/export/pdf")
    public void exportPdf(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            @RequestParam(required = false) Long serviceId,
            @RequestParam(required = false) Long quartierId,
            HttpServletResponse response) throws IOException {

        if (dateDebut == null) dateDebut = LocalDate.now().minusDays(30);
        if (dateFin == null) dateFin = LocalDate.now();

        try {
            // ✅ 1) Générer + sauvegarder
            Rapport rapport = dashboardService.exportPdfAndSave(
                    dateDebut, dateFin, serviceId, quartierId
            );

            // ✅ 2) Télécharger
            Path path = Paths.get(rapport.getCheminFichier());

            response.setContentType("application/pdf");
            response.setHeader(
                    "Content-Disposition",
                    "attachment; filename=" + rapport.getNomFichier()
            );

            Files.copy(path, response.getOutputStream());
            response.flushBuffer();

        } catch (Exception e) {
            response.reset();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Erreur export PDF : " + e.getMessage());
        }
    }

    /* ===================== AGENTS ===================== */

    @GetMapping("/agents")
    public String listAgents(Model model) {

        model.addAttribute(
                "agents",
                utilisateurRepository.findByRole(Role.AGENT)
        );

        return "admin/agents";
    }


    /**
     * API : Récupérer les agents d'un service (pour le modal)
     */
    @GetMapping("/services/{serviceId}/agents")
    @ResponseBody
    public List<AgentDto> getAgentsByService(@PathVariable Long serviceId) {
        var agents = utilisateurService.findAgentsByService(serviceId);
        return agents.stream()
                .map(agent -> new AgentDto(
                        agent.getId(),
                        agent.getPrenom(),
                        agent.getNom()
                ))
                .collect(Collectors.toList());
    }






    @GetMapping("/incidents/non-assignes")
    public String incidentsNonAssignes(Model model) {

        Utilisateur admin = currentUserService.getCurrentUser();

        // ✅ UTILISE LA NOUVELLE MÉTHODE
        List<Incident> incidents =
                incidentRepository.findIncidentsNonAssignesParDepartement(admin.getDepartement());

        model.addAttribute("admin", admin);
        model.addAttribute("incidents", incidents);
        model.addAttribute("services",
                serviceMunicipalRepository.findByDepartement(admin.getDepartement()));
        model.addAttribute("priorites", PrioriteIncident.values());

        return "admin/incidents-non-assignes";
    }
//detail dun incident
@GetMapping("/incidents/details/{id}")
public String detailsIncident(@PathVariable Long id, Model model) {

    Utilisateur admin = currentUserService.getCurrentUser();
    model.addAttribute("admin", admin);

    Incident incident = incidentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Incident non trouvé"));

    if (!incident.getDepartement().getId()
            .equals(admin.getDepartement().getId())) {
        throw new RuntimeException("Accès non autorisé");
    }

    model.addAttribute("incident", incident);
    return "admin/incident-details";
}

    /**
     * POST : Assigner un incident à un agent
     */
    @PostMapping("/incidents/{id}/assigner")
    public String assignerIncident(
            @PathVariable Long id,
            @RequestParam Long serviceId,
            @RequestParam Long agentId,
            @RequestParam PrioriteIncident priorite,
            @RequestParam(required = false) String commentaire,
            RedirectAttributes redirectAttributes) {

        try {
            incidentService.assignerIncident(id, serviceId, agentId,priorite);
            redirectAttributes.addFlashAttribute("success", "Incident assigné avec succès !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de l'assignation : " + e.getMessage());
        }

        return "redirect:/admin/dashboard";
    }





}

