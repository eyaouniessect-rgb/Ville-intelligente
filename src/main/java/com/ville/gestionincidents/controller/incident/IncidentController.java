package com.ville.gestionincidents.controller.incident;

import com.ville.gestionincidents.dto.incident.IncidentCreateDto;
import com.ville.gestionincidents.entity.Incident;
import com.ville.gestionincidents.entity.Utilisateur;
import com.ville.gestionincidents.enumeration.CategorieIncident;
import com.ville.gestionincidents.service.incident.IncidentService;
import com.ville.gestionincidents.service.utilisateur.UtilisateurService;

import com.ville.gestionincidents.security.CurrentUserService;


import com.ville.gestionincidents.repository.IncidentRepository;

import lombok.RequiredArgsConstructor;

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
    private final CurrentUserService currentUserService;

    /** Fonction utilitaire pour injecter l’utilisateur partout (Header + WebSocket) */
    private void injectUtilisateur(Model model) {
        Utilisateur utilisateur = currentUserService.getCurrentUser();
        if (utilisateur != null) {
            model.addAttribute("utilisateur", utilisateur);
        }
    }

    /** Page formulaire déclaration */
    @GetMapping("/getFormIncident")
    public String afficherFormulaire(Model model) {

        Utilisateur utilisateur = currentUserService.getCurrentUser();
        if (utilisateur == null) {
            return "redirect:/auth/login";
        }

        injectUtilisateur(model);

        model.addAttribute("incident", new IncidentCreateDto());
        model.addAttribute("categories", CategorieIncident.values());

        return "citoyen/incident_form";
    }

    /** Enregistrement de l'incident */
    @PostMapping("/incident/ajouter")
    public String ajouterIncident(@ModelAttribute IncidentCreateDto dto) {

        incidentService.creerIncident(dto);
        return "redirect:/citoyen/incident/success";
    }

    /** Page succès */
    @GetMapping("/incident/success")
    public String success(Model model) {

        injectUtilisateur(model);  // ⚠️ le plus important pour WebSocket

        return "citoyen/incident_success";
    }



}
