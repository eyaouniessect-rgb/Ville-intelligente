package com.ville.gestionincidents.controller.notification;

import com.ville.gestionincidents.dto.notification.PreferenceNotificationDTO;
import com.ville.gestionincidents.entity.Utilisateur;
import com.ville.gestionincidents.repository.UtilisateurRepository;
import com.ville.gestionincidents.service.notification.PreferenceNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/citoyen")
public class PreferenceNotificationController {

    private final PreferenceNotificationService preferenceService;
    private final UtilisateurRepository utilisateurRepository;

    /**
     * Affiche la page de préférences de notifications
     */
    @GetMapping("/preferences-notifications")
    public String afficherPreferences(Model model, Principal principal) {
        // Récupérer l'utilisateur connecté
        Utilisateur utilisateur = utilisateurRepository
                .findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Récupérer ou créer les préférences (retourne maintenant un DTO)
        PreferenceNotificationDTO pref = preferenceService.getOrCreate(utilisateur.getId());

        model.addAttribute("preferences", pref);

        return "citoyen/preferences_notifications";
    }

    /**
     * Met à jour les préférences de notifications
     */
    @PostMapping("/preferences-notifications")
    public String updatePreferences(
            @RequestParam(defaultValue = "false") boolean emailActif,
            @RequestParam(defaultValue = "false") boolean emailChangementStatut,
            @RequestParam(defaultValue = "false") boolean pushActif,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        // Récupérer l'utilisateur connecté
        Utilisateur utilisateur = utilisateurRepository
                .findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Récupérer les préférences existantes
        PreferenceNotificationDTO pref = preferenceService.getOrCreate(utilisateur.getId());

        // Mettre à jour les valeurs du DTO
        pref.setEmailActif(emailActif);
        pref.setEmailChangementStatut(emailChangementStatut);
        pref.setPushActif(pushActif);

        // Sauvegarder via le service
        preferenceService.updatePreferences(pref);

        redirectAttributes.addFlashAttribute(
                "message",
                "Préférences enregistrées avec succès"
        );

        return "redirect:/citoyen/preferences-notifications";
    }
}