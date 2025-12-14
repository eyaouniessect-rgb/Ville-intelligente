package com.ville.gestionincidents.controller.notification;

import com.ville.gestionincidents.entity.PreferenceNotification;
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

    @GetMapping("/preferences-notifications")
    public String afficherPreferences(Model model, Principal principal) {
        Utilisateur utilisateur = utilisateurRepository
                .findByEmail(principal.getName())
                .orElseThrow();

        PreferenceNotification pref = preferenceService.getOrCreate(utilisateur);
        model.addAttribute("preferences", pref);

        return "citoyen/preferences_notifications";
    }

    @PostMapping("/preferences-notifications")
    public String updatePreferences(
            @RequestParam(defaultValue = "false") boolean emailActif,
            @RequestParam(defaultValue = "false") boolean emailChangementStatut,
            @RequestParam(defaultValue = "false") boolean pushActif,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        Utilisateur utilisateur = utilisateurRepository
                .findByEmail(principal.getName())
                .orElseThrow();

        PreferenceNotification pref =
                preferenceService.getOrCreate(utilisateur);

        pref.setEmailActif(emailActif);
        pref.setEmailChangementStatut(emailChangementStatut);
        pref.setPushActif(pushActif);

        preferenceService.updatePreferences(pref);

        redirectAttributes.addFlashAttribute(
                "message",
                "Préférences enregistrées avec succès"
        );

        return "redirect:/citoyen/preferences-notifications";
    }


}