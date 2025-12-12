package com.ville.gestionincidents.controller.citoyen;

import com.ville.gestionincidents.dto.utilisateur.citoyen.ChangePasswordDto;
import com.ville.gestionincidents.dto.utilisateur.citoyen.CitoyenProfilDto;
import com.ville.gestionincidents.dto.utilisateur.citoyen.CitoyenUpdateProfilDto;
import com.ville.gestionincidents.entity.Incident;
import com.ville.gestionincidents.entity.Utilisateur;
import com.ville.gestionincidents.service.incident.IncidentService;
import com.ville.gestionincidents.service.notification.NotificationService;
import com.ville.gestionincidents.service.utilisateur.UtilisateurService;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

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
    // 📄 Affichage du profil
    @GetMapping("/profil")
    public String profil(Model model,
                         @AuthenticationPrincipal UserDetails userDetails) {

        CitoyenProfilDto profil =
                utilisateurService.getProfilCitoyen(userDetails.getUsername());

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

    //  Modification du profil
    @PostMapping("/profil")
    public String updateProfil(
            @Valid @ModelAttribute("utilisateur") CitoyenUpdateProfilDto dto,
            BindingResult result,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        if (result.hasErrors()) {
            return "citoyen/profil_citoyen";
        }

        String oldEmail = userDetails.getUsername();

        // 🟠 Si l’email a changé → page de confirmation
        if (!oldEmail.equals(dto.getEmail())) {
            model.addAttribute("ancienEmail", oldEmail);
            model.addAttribute("nouvelEmail", dto.getEmail());
            model.addAttribute("dto", dto); // on garde les données
            return "citoyen/confirm_email_change";
        }

        // 🟢 Sinon, modification normale
        utilisateurService.updateProfilCitoyen(oldEmail, dto);
        return "redirect:/citoyen/profil";
    }

    @PostMapping("/profil/confirm")
    public String confirmEmailChange(
            @ModelAttribute CitoyenUpdateProfilDto dto,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        String oldEmail = userDetails.getUsername();

        // sauvegarde réelle
        utilisateurService.updateProfilCitoyen(oldEmail, dto);

        // déconnexion
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
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "citoyen/change_password";
        }

        Utilisateur user =
                utilisateurService.findByEmail(userDetails.getUsername());

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
