package com.ville.gestionincidents.controller.incident;

import com.ville.gestionincidents.dto.incident.IncidentCreateDto;
import com.ville.gestionincidents.entity.Incident;
import com.ville.gestionincidents.entity.Utilisateur;
import com.ville.gestionincidents.enumeration.CategorieIncident;
import com.ville.gestionincidents.service.incident.IncidentService;
import com.ville.gestionincidents.service.utilisateur.UtilisateurService;
import com.ville.gestionincidents.repository.IncidentRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/citoyen")
public class IncidentController {

    private final IncidentService incidentService;
    private final UtilisateurService utilisateurService;

    /**
     * Affiche le formulaire de déclaration
     */
    @GetMapping("/getFormIncident")
    public String afficherFormulaire(Model model, Authentication authentication) {

        // 🔐 Sécurité minimale
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/auth/login";
        }

        // 📧 Email depuis OAuth2 / login classique
        String email = authentication.getName();

        // 👤 Charger utilisateur métier pour la vue
        Utilisateur utilisateur = utilisateurService.findByEmail(email);

        // 📦 Données pour Thymeleaf
        model.addAttribute("utilisateur", utilisateur);
        model.addAttribute("incident", new IncidentCreateDto());
        model.addAttribute("categories", CategorieIncident.values());

        return "citoyen/incident_form";
    }

    /**
     * Traite la soumission du formulaire
     */
    @PostMapping("/incident/ajouter")
    public String ajouterIncident(@ModelAttribute IncidentCreateDto dto) {

        // ✅ Le service récupère l'utilisateur via CurrentUserService
        incidentService.creerIncident(dto);

        return "redirect:/citoyen/incident/success";
    }

    /**
     * Page succès
     */
    @GetMapping("/incident/success")
    public String success() {
        return "citoyen/incident_success";
    }



}
