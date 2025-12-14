package com.ville.gestionincidents.controller.citoyen;

import com.ville.gestionincidents.dto.utilisateur.citoyen.ChangePasswordDto;
import com.ville.gestionincidents.dto.utilisateur.citoyen.CitoyenProfilDto;
import com.ville.gestionincidents.dto.utilisateur.citoyen.CitoyenUpdateProfilDto;
import com.ville.gestionincidents.entity.Incident;
import com.ville.gestionincidents.entity.Utilisateur;
import com.ville.gestionincidents.security.AuthenticationHelper;
import com.ville.gestionincidents.service.incident.IncidentService;
import com.ville.gestionincidents.service.notification.NotificationService;
import com.ville.gestionincidents.service.utilisateur.UtilisateurService;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequestMapping("/citoyen")
@RequiredArgsConstructor
public class CitoyenController {

    private final IncidentService incidentService;
    private final NotificationService notificationService;
    private final UtilisateurService utilisateurService;
    private final AuthenticationHelper authHelper; //

    // -------------------------
    // Dashboard Citoyen
    // -------------------------
    @GetMapping("/home")
    public String dashboard(Model model, Authentication authentication) {

        String email = authHelper.getEmailOrThrow(authentication);
        Utilisateur utilisateur = utilisateurService.findByEmail(email);

        //  Ajouter l'utilisateur au modèle (pour le header)
        model.addAttribute("utilisateur", utilisateur);

        model.addAttribute("countSignale", incidentService.countSignale(email));
        model.addAttribute("countPrisEnCharge", incidentService.countPrisEnCharge(email));
        model.addAttribute("countEnResolution", incidentService.countEnResolution(email));
        model.addAttribute("countResolu", incidentService.countResolu(email));
        model.addAttribute("countCloture", incidentService.countCloture(email));

        return "citoyen/home";
    }

    // -------------------------
    // Liste incidents
    // -------------------------
    @GetMapping("/incidents")
    public String incidentsList(
            @RequestParam(value = "statut", required = false) String statut,
            Model model,
            Authentication authentication) {

        String email = authHelper.getEmailOrThrow(authentication);
        Utilisateur utilisateur = utilisateurService.findByEmail(email);

        // ✅ Ajouter l'utilisateur au modèle (pour le header)
        model.addAttribute("utilisateur", utilisateur);

        boolean isFiltering = (statut != null && !statut.isEmpty());

        if (!isFiltering) {
            model.addAttribute("incidents", incidentService.getIncidentsForCurrentUser());
        } else {
            model.addAttribute("incidents", incidentService.getIncidentsByStatutForUser(email, statut));
        }

        boolean hasAnyIncident = !incidentService.getIncidentsForCurrentUser().isEmpty();

        model.addAttribute("hasAnyIncident", hasAnyIncident);
        model.addAttribute("isFiltering", isFiltering);
        model.addAttribute("statuts", com.ville.gestionincidents.enumeration.StatutIncident.values());
        model.addAttribute("statutActuel", statut);

        return "citoyen/incidents-list";
    }

    // -------------------------
    // Détails incident
    // -------------------------
    @GetMapping("/incidents/{id}")
    public String incidentDetails(@PathVariable Long id,
                                  Model model,
                                  Authentication authentication) {

        String email = authHelper.getEmailOrThrow(authentication);
        Utilisateur utilisateur = utilisateurService.findByEmail(email);

        // ✅ Ajouter l'utilisateur au modèle (pour le header)
        model.addAttribute("utilisateur", utilisateur);

        Incident inc = incidentService.findByIdAndCheckOwner(id, email);
        model.addAttribute("incident", inc);

        return "citoyen/incident-details";
    }

    // -------------------------
    // Profil Citoyen
    // -------------------------
    @GetMapping("/profil")
    public String profil(Model model, Authentication authentication) {

        String email = authHelper.getEmailOrThrow(authentication);
        CitoyenProfilDto profil = utilisateurService.getProfilCitoyen(email);

        CitoyenUpdateProfilDto form = new CitoyenUpdateProfilDto();
        form.setNom(profil.getNom());
        form.setPrenom(profil.getPrenom());
        form.setEmail(profil.getEmail());
        form.setTelephone(profil.getTelephone());
        form.setAdresse(profil.getAdresse());

        model.addAttribute("profil", profil);
        model.addAttribute("utilisateur", form);

        return "citoyen/profil_citoyen";
    }

    @PostMapping("/profil")
    public String updateProfil(
            @Valid @ModelAttribute("utilisateur") CitoyenUpdateProfilDto dto,
            BindingResult result,
            Authentication authentication,
            Model model) {

        if (result.hasErrors()) {
            return "citoyen/profil_citoyen";
        }

        String oldEmail = authHelper.getEmailOrThrow(authentication);

        // 🟠 Si l'email a changé → page de confirmation
        if (!oldEmail.equals(dto.getEmail())) {
            model.addAttribute("ancienEmail", oldEmail);
            model.addAttribute("nouvelEmail", dto.getEmail());
            model.addAttribute("dto", dto);
            return "citoyen/confirm_email_change";
        }

        // 🟢 Sinon, modification normale
        utilisateurService.updateProfilCitoyen(oldEmail, dto);
        return "redirect:/citoyen/profil";
    }

    @PostMapping("/profil/confirm")
    public String confirmEmailChange(
            @ModelAttribute CitoyenUpdateProfilDto dto,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        String oldEmail = authHelper.getEmailOrThrow(authentication);

        utilisateurService.updateProfilCitoyen(oldEmail, dto);

        SecurityContextHolder.clearContext();

        redirectAttributes.addFlashAttribute(
                "toast",
                "Email modifié. Veuillez vous reconnecter."
        );

        return "redirect:/login";
    }

    @GetMapping("/change-password")
    public String changePasswordForm(Model model) {
        model.addAttribute("passwordDto", new ChangePasswordDto());
        return "citoyen/change_password";
    }

    @PostMapping("/change-password")
    public String changePassword(
            @Valid @ModelAttribute("passwordDto") ChangePasswordDto dto,
            BindingResult result,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "citoyen/change_password";
        }

        String email = authHelper.getEmailOrThrow(authentication);
        Utilisateur user = utilisateurService.findByEmail(email);

        try {
            utilisateurService.changePasswordCitoyen(user.getId(), dto);
            redirectAttributes.addFlashAttribute(
                    "success",
                    "Mot de passe modifié avec succès"
            );
            return "redirect:/citoyen/profil";

        } catch (RuntimeException e) {
            result.reject(null, e.getMessage());
            return "citoyen/change_password";
        }
    }
}