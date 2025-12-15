package com.ville.gestionincidents.controller.citoyen;

import com.ville.gestionincidents.dto.incident.IncidentDetailsDto;
import com.ville.gestionincidents.dto.incident.IncidentListDto;
import com.ville.gestionincidents.dto.utilisateur.citoyen.ChangePasswordDto;
import com.ville.gestionincidents.dto.utilisateur.citoyen.CitoyenProfilDto;
import com.ville.gestionincidents.dto.utilisateur.citoyen.CitoyenUpdateProfilDto;
import com.ville.gestionincidents.entity.Utilisateur;
import com.ville.gestionincidents.enumeration.StatutIncident;
import com.ville.gestionincidents.security.AuthenticationHelper;
import com.ville.gestionincidents.security.CurrentUserService;
import com.ville.gestionincidents.service.incident.IncidentService;
import com.ville.gestionincidents.service.utilisateur.UtilisateurService;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

import javax.validation.Valid;

@Controller
@RequestMapping("/citoyen")
@RequiredArgsConstructor
public class CitoyenController {

    private final IncidentService incidentService;
    private final UtilisateurService utilisateurService;
    private final AuthenticationHelper authHelper; //
    private final CurrentUserService currentUserService;

    // -------------------------
    // Dashboard Citoyen
    // -------------------------
    @GetMapping("/home")
    public String dashboard(Model model, Authentication authentication) {

        Utilisateur utilisateur = currentUserService.getCurrentUser();

        //  Ajouter l'utilisateur au modèle (pour le header)
        model.addAttribute("utilisateur", utilisateur);

        model.addAttribute("countSignale", incidentService.countSignaleForCurrentUser());
        model.addAttribute("countPrisEnCharge", incidentService.countPrisEnChargeForCurrentUser());
        model.addAttribute("countEnResolution", incidentService.countEnResolutionForCurrentUser());
        model.addAttribute("countResolu", incidentService.countResoluForCurrentUser());
        model.addAttribute("countCloture", incidentService.countClotureForCurrentUser());

        return "citoyen/home";
    }


    // -------------------------
    // Liste incidents
    // -------------------------

    //receuperer la liste des incidents pour un utlisateur connecte selon un statut ( filtre ) par defaut recupere tous les status
    @GetMapping("/incidents")
    public String incidentsList(
            @RequestParam(value = "statut", required = false) String statut,
            Model model,
            Authentication authentication) {

        Utilisateur utilisateur = currentUserService.getCurrentUser();

        // ✅ Ajouter l'utilisateur au modèle (pour le header)
        model.addAttribute("utilisateur", utilisateur);
        boolean hasStatutParam = (statut != null && !statut.isEmpty());

        StatutIncident statutFiltre = null;
        if (hasStatutParam) {
            try {
                statutFiltre = StatutIncident.valueOf(statut);
            } catch (IllegalArgumentException e) {
                statutFiltre = null; // statut invalide -> pas de filtre
            }
        }

        // 🔹 Liste filtrée OU non filtrée
        List<IncidentListDto> allIncidents = incidentService.getIncidentsForCurrentUser();
        List<IncidentListDto> incidents = (statutFiltre == null)
                ? allIncidents
                : incidentService.getIncidentsByStatutForCurrentUser(statutFiltre);

        // 🔹 Vérifier SI l'utilisateur possède AU MOINS un incident (tous statuts confondus)
        boolean hasAnyIncident = !allIncidents.isEmpty();

        model.addAttribute("incidents", incidents);
        model.addAttribute("hasAnyIncident", hasAnyIncident);
        model.addAttribute("isFiltering", statutFiltre != null);
        model.addAttribute("statuts", StatutIncident.values());
        model.addAttribute("statutActuel", statutFiltre != null ? statutFiltre.name() : null);

        return "citoyen/incidents-list";
    }




    // -------------------------
    // Détails incident
    // -------------------------
    @GetMapping("/incidents/{id}")
    public String incidentDetails(@PathVariable Long id,
                                  Model model,
                                  Authentication authentication) {
        Utilisateur utilisateur = currentUserService.getCurrentUser();

        IncidentDetailsDto inc = incidentService.getIncidentDetailsForCurrentUser(id);

        model.addAttribute("incident", inc);
        // ✅ Ajouter l'utilisateur au modèle (pour le header)
        model.addAttribute("utilisateur", utilisateur);
        return "citoyen/incident-details";
    }






    // -------------------------
    // Profil Citoyen
    // -------------------------
    // 📄 Affichage du profil
    @GetMapping("/profil")
    public String profil(Model model, Authentication authentication) {

        Utilisateur utilisateur = currentUserService.getCurrentUser();
        String email = utilisateur.getEmail();
        
        CitoyenProfilDto profil = utilisateurService.getProfilCitoyen(email);

        CitoyenUpdateProfilDto form = new CitoyenUpdateProfilDto();
        form.setNom(profil.getNom());
        form.setPrenom(profil.getPrenom());
        form.setEmail(profil.getEmail());
        form.setTelephone(profil.getTelephone());
        form.setAdresse(profil.getAdresse());

        model.addAttribute("profil", profil);
        model.addAttribute("utilisateur", utilisateur); // Entité Utilisateur pour le header
        model.addAttribute("utilisateurForm", form); // DTO pour le formulaire

        return "citoyen/profil_citoyen";
    }

    //  Modification du profil
    @PostMapping("/profil")
    public String updateProfil(
            @Valid @ModelAttribute("utilisateur") CitoyenUpdateProfilDto dto,
            BindingResult result,
            Authentication authentication,
            Model model){

        if (result.hasErrors()) {
            return "citoyen/profil_citoyen";
        }

        String oldEmail = authHelper.getEmailOrThrow(authentication);

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
