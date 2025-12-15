package com.ville.gestionincidents.controller.admin;

import com.ville.gestionincidents.entity.Incident;
import com.ville.gestionincidents.entity.ServiceMunicipal;
import com.ville.gestionincidents.enumeration.PrioriteIncident;
import com.ville.gestionincidents.repository.UtilisateurRepository;
import com.ville.gestionincidents.service.dashboardAdmin.DashboardService;
import com.ville.gestionincidents.security.CurrentUserService;
import com.ville.gestionincidents.service.incident.IncidentService;
import com.ville.gestionincidents.service.serviceMunicipal.ServiceMunicipalService;
import com.ville.gestionincidents.enumeration.StatutIncident;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.ville.gestionincidents.service.utilisateur.UtilisateurService;
import com.ville.gestionincidents.enumeration.Role;
import java.util.List;
import java.util.stream.Collectors;
import com.ville.gestionincidents.dto.utilisateur.agent.AgentDto;
import com.ville.gestionincidents.repository.IncidentRepository;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.ville.gestionincidents.enumeration.PrioriteIncident;
import com.ville.gestionincidents.repository.ServiceMunicipalRepository;


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


    /**
     * PAGE 1 : Dashboard principal avec la liste des incidents
     */
    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(required = false) Long serviceId,
            @RequestParam(required = false) String statut,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dateDebut,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dateFin,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        // Récupérer l'admin connecté
        var admin = currentUserService.getCurrentUser();
        var departement = admin.getDepartement();

        // Si pas de dates, prendre les 30 derniers jours
        if (dateDebut == null) {
            dateDebut = LocalDate.now().minusDays(30);
        }
        if (dateFin == null) {
            dateFin = LocalDate.now();
        }

        // Statistiques du département
        model.addAttribute("admin", admin);
        model.addAttribute("totalIncidents",
                incidentService.countByDepartement(departement));
        model.addAttribute("incidentsResolus",
                incidentService.countByDepartementAndStatut(departement, StatutIncident.RESOLU));
        model.addAttribute("incidentsEnCours",
                incidentService.countByDepartementAndStatutsEnCours(departement));
        model.addAttribute("totalAgents",
                utilisateurService.countAgentsByDepartement(departement));

        model.addAttribute("incidentsNonAssignes",
                incidentService.countNonAssignesByDepartement(departement)
);

        // Services du département
        model.addAttribute("services",
                serviceMunicipalService.findByDepartement(departement));

        // Liste des incidents avec filtres
        var incidents = incidentService.findByDepartementWithFilters(
                departement, serviceId, statut, dateDebut, dateFin, page, 20);
        model.addAttribute("incidents", incidents.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", incidents.getTotalPages());

        // Garder les filtres
        model.addAttribute("serviceId", serviceId);
        model.addAttribute("statut", statut);
        model.addAttribute("dateDebut", dateDebut);
        model.addAttribute("dateFin", dateFin);
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

            Model model){

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

        // Statistiques générales
        model.addAttribute("totalIncidents",
                dashboardService.countTotalIncidentsByDepartement(departement, dateDebut, dateFin));
        model.addAttribute("incidentsResolus",
                dashboardService.countIncidentsResolusByDepartement(departement, dateDebut, dateFin));
        model.addAttribute("incidentsEnCours",
                dashboardService.countIncidentsEnCoursByDepartement(departement, dateDebut, dateFin));
        model.addAttribute("delaiMoyenResolution",
                dashboardService.calculerDelaiMoyenResolutionByDepartement(departement, dateDebut, dateFin));

        // Données pour les graphiques
        model.addAttribute("incidentsParService",
                dashboardService.getIncidentsParServiceByDepartement(departement, dateDebut, dateFin));
        model.addAttribute("incidentsParQuartier",
                dashboardService.getIncidentsParQuartierByDepartement(departement, dateDebut, dateFin));
        model.addAttribute("delaiResolutionParService",
                dashboardService.getDelaiResolutionParServiceByDepartement(departement, dateDebut, dateFin));
        model.addAttribute("incidentsParStatut",
                dashboardService.getIncidentsParStatutByDepartement(departement, dateDebut, dateFin));
        model.addAttribute("incidentsParMois",
                dashboardService.getIncidentsParMoisByDepartement(departement, dateDebut, dateFin));

        // Filtres de date
        model.addAttribute("dateDebut", dateDebut);
        model.addAttribute("dateFin", dateFin);

        return "admin/rapport_analytique";
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
            incidentService.assignerIncident(id, serviceId, agentId, commentaire,priorite);
            redirectAttributes.addFlashAttribute("success", "Incident assigné avec succès !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de l'assignation : " + e.getMessage());
        }

        return "redirect:/admin/dashboard";
    }

    /**
     * Export CSV des statistiques
     */
    @GetMapping("/dashboard/export/csv")
    public void exportCsv(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dateDebut,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dateFin,
            HttpServletResponse response) throws IOException {

        if (dateDebut == null) dateDebut = LocalDate.now().minusDays(30);
        if (dateFin == null) dateFin = LocalDate.now();

        response.setContentType("text/csv");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename=rapport_incidents_" + LocalDate.now() + ".csv");

        try {
            dashboardService.exportCsv(dateDebut, dateFin, response.getWriter());
        } catch (Exception e) {
            response.reset();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Erreur lors de l'export CSV : " + e.getMessage());
        }
    }


    /**
     * Export PDF des statistiques
     */
    @GetMapping("/dashboard/export/pdf")
    public void exportPdf(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dateDebut,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dateFin,
            HttpServletResponse response) throws IOException {

        if (dateDebut == null) dateDebut = LocalDate.now().minusDays(30);
        if (dateFin == null) dateFin = LocalDate.now();

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition",
                "attachment; filename=rapport_incidents_" + LocalDate.now() + ".pdf");

        try {
            dashboardService.exportPdf(dateDebut, dateFin, response.getOutputStream());
        } catch (Exception e) {
            response.reset();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Erreur lors de l'export PDF : " + e.getMessage());
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


    @GetMapping("/incidents/signales")
    public String incidentsNonAssignes(
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        // Récupérer l'admin connecté
        var admin = currentUserService.getCurrentUser();
        model.addAttribute("admin", admin);

        // Récupérer les incidents non assignés (avec pagination optionnelle)
        List<Incident> incidents = incidentRepository.findByAgentIsNull();
        model.addAttribute("incidents", incidents);

        // ✅ AJOUTEZ CES LIGNES : Charger les services et priorités pour le modal
        List<ServiceMunicipal> services = serviceMunicipalRepository
                .findByDepartement(admin.getDepartement());
        model.addAttribute("services", services);
        model.addAttribute("priorites",PrioriteIncident.values());

        // Pagination
        model.addAttribute("totalPages", 1);
        model.addAttribute("currentPage", 0);

        return "admin/incidents-non-assignes";
    }
//detail dun incident
    @GetMapping("/incidents/{id}")
    public String detailsIncident(@PathVariable Long id, Model model) {

        // Récupérer l'admin connecté
        var admin = currentUserService.getCurrentUser();
        model.addAttribute("admin", admin);

        // Récupérer l'incident
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Incident non trouvé"));

        // Vérifier que l'incident appartient au département de l'admin via le service
        if (incident.getService() == null ||
                !incident.getService().getDepartement().getId().equals(admin.getDepartement().getId())) {
            throw new RuntimeException("Accès non autorisé");
        }

        model.addAttribute("incident", incident);

        return "admin/incident-details";
    }

}

