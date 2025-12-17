package com.ville.gestionincidents.controller.agent;

import com.ville.gestionincidents.entity.Incident;
import com.ville.gestionincidents.entity.Utilisateur;
import com.ville.gestionincidents.enumeration.StatutIncident;
import com.ville.gestionincidents.enumeration.TypeNotification;
import com.ville.gestionincidents.security.CurrentUserService;
import com.ville.gestionincidents.service.incident.IncidentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

import com.ville.gestionincidents.repository.IncidentRepository;
import com.ville.gestionincidents.repository.UtilisateurRepository;
import com.ville.gestionincidents.service.notification.NotificationService;
import com.ville.gestionincidents.service.email.EmailService;

@Controller
@RequestMapping("/agent")
@RequiredArgsConstructor
public class AgentController {

    private final IncidentService incidentService;
    private final CurrentUserService currentUserService;
    private final IncidentRepository incidentRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;

    // ===================== DASHBOARD =====================

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Récupérer l'agent connecté
        Utilisateur agent = currentUserService.getCurrentUser();

        // Ajouter l'agent au modèle
        model.addAttribute("agent", agent);

        // Statistiques par statut
        long totalIncidents = incidentService.countByAgent(agent);
        model.addAttribute("totalIncidents", totalIncidents);

        model.addAttribute("countSignale",
                incidentService.countByAgentAndStatut(agent, StatutIncident.SIGNALE));

        model.addAttribute("countPrisEnCharge",
                incidentService.countByAgentAndStatut(agent, StatutIncident.PRIS_EN_CHARGE));

        model.addAttribute("countEnResolution",
                incidentService.countByAgentAndStatut(agent, StatutIncident.EN_RESOLUTION));

        model.addAttribute("countResolu",
                incidentService.countByAgentAndStatut(agent, StatutIncident.RESOLU));

        model.addAttribute("countCloture",
                incidentService.countByAgentAndStatut(agent, StatutIncident.CLOTURE));

        // Liste de tous les incidents assignés à l'agent (non clôturés)
        var incidents = incidentService.findByAgent(agent)
                .stream()
                .filter(i -> i.getStatut() != StatutIncident.CLOTURE)
                .toList();

        model.addAttribute("incidents", incidents);

        return "agent/dashboard";
    }

    // ===================== CHANGER STATUT =====================
    @PostMapping("/incidents/{id}/changer-statut")
    public String changerStatutIncident(
            @PathVariable Long id,
            @RequestParam StatutIncident statut,
            RedirectAttributes redirectAttributes
    ) {

        incidentService.changerStatut(id, statut);

        redirectAttributes.addFlashAttribute(
                "success",
                "Statut de l'incident mis à jour avec succès"
        );

        return "redirect:/agent/dashboard";
    }


    // ===================== LISTE DES INCIDENTS (optionnel) =====================

    @GetMapping("/incidents")
    public String listeIncidents(
            @RequestParam(required = false) StatutIncident statut,
            Model model) {

        Utilisateur agent = currentUserService.getCurrentUser();
        model.addAttribute("agent", agent);

        var incidents = (statut != null)
                ? incidentService.findByAgentAndStatut(agent, statut)
                : incidentService.findByAgent(agent);

        model.addAttribute("incidents", incidents);
        model.addAttribute("statutFiltre", statut);

        return "agent/incidents";
    }

    // ===================== DÉTAILS D'UN INCIDENT (optionnel) =====================

    @GetMapping("/incidents/{id}")
    public String detailsIncident(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            Utilisateur agent = currentUserService.getCurrentUser();

            // Récupérer l'incident et vérifier que l'agent est bien assigné
            var incident = incidentService.findById(id);

            if (!incident.getAgent().getId().equals(agent.getId())) {
                redirectAttributes.addFlashAttribute("error",
                        "Vous n'êtes pas autorisé à voir cet incident");
                return "redirect:/agent/dashboard";
            }

            model.addAttribute("agent", agent);
            model.addAttribute("incident", incident);

            return "agent/incident-details";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Incident introuvable : " + e.getMessage());
            return "redirect:/agent/dashboard";
        }
    }
}