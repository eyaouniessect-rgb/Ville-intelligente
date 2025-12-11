package com.ville.gestionincidents.controller.citoyen;

import com.ville.gestionincidents.entity.Incident;
import com.ville.gestionincidents.entity.Utilisateur;
import com.ville.gestionincidents.service.incident.IncidentService;
import com.ville.gestionincidents.service.notification.NotificationService;
import com.ville.gestionincidents.service.utilisateur.UtilisateurService;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/citoyen")
@RequiredArgsConstructor
public class CitoyenController {

    private final IncidentService incidentService;
    private final NotificationService notificationService;
    private final UtilisateurService utilisateurService;

    // -------------------------
    // Dashboard Citoyen
    // -------------------------
    @GetMapping("/home")
    public String dashboard(Model model,
                            @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails.getUsername();

        model.addAttribute("totalIncidents", incidentService.countByEmail(email));
        model.addAttribute("inProgress", incidentService.countInProgress(email));
        model.addAttribute("resolved", incidentService.countResolved(email));

        return "citoyen/home";
    }


    // -------------------------
    // Liste incidents
    // -------------------------
    @GetMapping("/incidents")
    public String incidentsList(Model model,
                                @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails.getUsername();

        model.addAttribute("incidents", incidentService.findByCitoyenEmail(email));

        return "citoyen/incidents-list";
    }


    // -------------------------
    // Détails incident
    // -------------------------
    @GetMapping("/incidents/{id}")
    public String incidentDetails(@PathVariable Long id,
                                  Model model,
                                  @AuthenticationPrincipal UserDetails userDetails) {

        Incident inc = incidentService.findByIdAndCheckOwner(id, userDetails.getUsername());

        model.addAttribute("incident", inc);

        return "citoyen/incident-details";
    }


    // -------------------------
    // Notifications
    // -------------------------
    @GetMapping("/notifications")
    public String notifications(Model model,
                                @AuthenticationPrincipal UserDetails userDetails) {

        model.addAttribute("notifications",
                notificationService.findByUserEmail(userDetails.getUsername()));

        return "citoyen/notifi_list";
    }


    // -------------------------
    // Profil Citoyen
    // -------------------------
    @GetMapping("/profil")
    public String profil(Model model,
                         @AuthenticationPrincipal UserDetails userDetails) {

        Utilisateur u = utilisateurService.findByEmail(userDetails.getUsername());

        model.addAttribute("citoyen", u);

        return "citoyen/profil_citoyen";
    }
}
