package com.ville.gestionincidents.controller.incident;

import com.ville.gestionincidents.dto.incident.IncidentCreateDto;
import com.ville.gestionincidents.entity.Incident;
import com.ville.gestionincidents.entity.Utilisateur;
import com.ville.gestionincidents.enumeration.CategorieIncident;
import com.ville.gestionincidents.service.incident.IncidentService;
import com.ville.gestionincidents.service.utilisateur.UtilisateurService;
import com.ville.gestionincidents.security.CurrentUserService;
import com.ville.gestionincidents.repository.IncidentRepository;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/citoyen")
public class IncidentController {

    private final IncidentService incidentService;
    private final UtilisateurService utilisateurService;
    private final CurrentUserService currentUserService;

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

    @PostMapping("/incident/ajouter")
    public String ajouterIncident(
            @Valid @ModelAttribute("incident") IncidentCreateDto dto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        // 🔹 Sécurité : utilisateur connecté
        Utilisateur utilisateur = currentUserService.getCurrentUser();

        if (utilisateur == null) {
            return "redirect:/auth/login";
        }
        model.addAttribute("utilisateur", currentUserService.getCurrentUser());


        // 🔹 Nettoyage photos vides
        if (dto.getPhotos() != null) {
            dto.setPhotos(
                    dto.getPhotos().stream()
                            .filter(f -> f != null && !f.isEmpty())
                            .toList()
            );
        }

        // 🔹 Validation max photos
        if (dto.getPhotos() != null && dto.getPhotos().size() > 3) {
            bindingResult.rejectValue("photos", "photos.max",
                    "Maximum 3 photos autorisées");
        }

        // 🔹 S’il y a des erreurs → rester sur le formulaire
        if (bindingResult.hasErrors()) {
            injectUtilisateur(model);
            model.addAttribute("categories", CategorieIncident.values());
            return "citoyen/incident_form";
        }

        // 🔹 Création incident
        incidentService.creerIncident(dto);

        // 🔹 Message flash (1 seule fois)
        redirectAttributes.addFlashAttribute(
                "successMessage",
                "✅ Incident déclaré avec succès"
        );

        // ✅ NO REDIRECT → rester sur la page
        // Option 1 : formulaire vide
        model.addAttribute("incident", new IncidentCreateDto());

        return "citoyen/incident_form";
    }

}