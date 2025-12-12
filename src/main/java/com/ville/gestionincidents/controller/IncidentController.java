package com.ville.gestionincidents.controller;

import com.ville.gestionincidents.dto.incident.IncidentCreateDto;
import com.ville.gestionincidents.enumeration.CategorieIncident;
import com.ville.gestionincidents.service.incident.IncidentService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/incident")
public class IncidentController {

    private final IncidentService incidentService;

    /**
     * Affiche le formulaire de déclaration
     */
    @GetMapping("/nouveau")
    public String afficherFormulaire(Model model) {
        model.addAttribute("incident", new IncidentCreateDto());
        model.addAttribute("categories", CategorieIncident.values());
        return "citoyen/incident_form";
    }

    /**
     * Traite la soumission du formulaire
     */
    @PostMapping("/ajouter")
    public String ajouterIncident(@ModelAttribute IncidentCreateDto dto) {

        incidentService.creerIncident(dto);

        return "redirect:/incident/success";
    }

    /**
     * Page succès
     */
    @GetMapping("/success")
    public String success() {
        return "citoyen/incident_success";
    }
}
