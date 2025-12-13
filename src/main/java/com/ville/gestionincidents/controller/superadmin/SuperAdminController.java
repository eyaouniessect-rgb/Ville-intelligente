package com.ville.gestionincidents.controller.superadmin;

import com.ville.gestionincidents.dto.utilisateur.superAdmin.CreateUtilisateurByAdminDto;
import com.ville.gestionincidents.dto.utilisateur.superAdmin.UpdateUtilisateurByAdminDto;
import com.ville.gestionincidents.entity.Utilisateur;
import com.ville.gestionincidents.enumeration.Role;
import com.ville.gestionincidents.service.departement.DepartementService;
import com.ville.gestionincidents.service.utilisateur.UtilisateurService;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * ✅ CONTROLLER REFACTORISÉ AVEC DTOs
 *
 * Contrôleur SuperAdmin pour la gestion complète des utilisateurs
 * - Utilise des DTOs pour la création et modification
 * - Validation automatique via @Valid
 * - Séparation claire des responsabilités
 */
@Controller
@RequestMapping("/superadmin")
@PreAuthorize("hasRole('SUPERADMIN')") // ⚠️ Accès réservé au SUPERADMIN uniquement
public class SuperAdminController {

    @Autowired
    private UtilisateurService utilisateurService;
    @Autowired
    private DepartementService departementService;

    // ==================== DASHBOARD ====================

    /**
     * Affiche le tableau de bord avec les statistiques
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalUsers", utilisateurService.countAllUsers());
        model.addAttribute("totalAdmins", utilisateurService.countByRole(Role.ADMIN));
        model.addAttribute("totalAgents", utilisateurService.countByRole(Role.AGENT));
        model.addAttribute("totalCitoyens", utilisateurService.countByRole(Role.CITOYEN));
        model.addAttribute("recentUsers", utilisateurService.findRecentUsers(5));

        return "superadmin/dashboard";
    }

    // ==================== LISTE DES UTILISATEURS ====================

    /**
     * Affiche la liste des utilisateurs (avec filtre optionnel par rôle)
     */
    @GetMapping("/users")
    public String listUsers(@RequestParam(required = false) Role role, Model model) {
        List<Utilisateur> users;

        if (role != null) {
            users = utilisateurService.findByRole(role);
            model.addAttribute("filterRole", role);
        } else {
            users = utilisateurService.findAllExceptSuperAdmin();
        }

        model.addAttribute("users", users);
        model.addAttribute("roles", Role.values());
        return "superadmin/users";
    }

    // ==================== CRÉER UN ADMINISTRATEUR (REFACTORISÉ) ====================

    /**
     * ✅ REFACTORISÉ : Utilise maintenant un DTO
     * Affiche le formulaire de création d'admin
     */
    @GetMapping("/create-admin")
    public String createAdminForm(Model model) {

        model.addAttribute("utilisateur", new CreateUtilisateurByAdminDto());

        // ✅ liste des départements existants
        model.addAttribute("departements", departementService.findAll());

        return "superadmin/create-admin";
    }


    /**
     * ✅ REFACTORISÉ : Utilise maintenant un DTO avec validation
     * Traite la soumission du formulaire de création d'admin
     */
    @PostMapping("/create-admin")
    public String createAdmin(@Valid @ModelAttribute("utilisateur") CreateUtilisateurByAdminDto dto,
                              BindingResult result,
                              RedirectAttributes redirectAttributes) {
        // 1. Vérifier les erreurs de validation
        if (result.hasErrors()) {
            // Les erreurs sont automatiquement disponibles dans la vue
            return "superadmin/create-admin";
        }

        try {
            // 2. Créer l'admin via le service (qui utilise le mapper)
            utilisateurService.createUserByAdmin(dto, Role.ADMIN);

            // 3. Message de succès
            redirectAttributes.addFlashAttribute("success",
                    "Administrateur '" + dto.getEmail() + "' créé avec succès");
            return "redirect:/superadmin/users?role=ADMIN";

        } catch (Exception e) {
            // 4. Gestion des erreurs métier
            redirectAttributes.addFlashAttribute("error",
                    "Erreur lors de la création : " + e.getMessage());
            return "redirect:/superadmin/create-admin";
        }
    }

    // ==================== CRÉER UN AGENT (REFACTORISÉ) ====================

    /**
     * ✅ REFACTORISÉ : Utilise maintenant un DTO
     * Affiche le formulaire de création d'agent
     */
    @GetMapping("/create-agent")
    public String createAgentForm(Model model) {

        model.addAttribute("utilisateur", new CreateUtilisateurByAdminDto());

        // ✅ EXACTEMENT comme pour admin
        model.addAttribute("departements", departementService.findAll());

        return "superadmin/create-agent";
    }

    /**
     * ✅ REFACTORISÉ : Utilise maintenant un DTO avec validation
     * Traite la soumission du formulaire de création d'agent
     */
    @PostMapping("/create-agent")
    public String createAgent(@Valid @ModelAttribute("utilisateur") CreateUtilisateurByAdminDto dto,
                              BindingResult result,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "superadmin/create-agent";
        }

        try {
            // Créer l'agent avec le rôle AGENT
            utilisateurService.createUserByAdmin(dto, Role.AGENT);

            redirectAttributes.addFlashAttribute("success",
                    "Agent '" + dto.getEmail() + "' créé avec succès");
            return "redirect:/superadmin/users?role=AGENT";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Erreur lors de la création : " + e.getMessage());
            return "redirect:/superadmin/create-agent";
        }
    }

    // ==================== VOIR DÉTAILS D'UN UTILISATEUR ====================

    /**
     * Affiche les détails complets d'un utilisateur
     */
    @GetMapping("/users/{id}")
    public String viewUser(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Utilisateur user = utilisateurService.findById(id);
            model.addAttribute("user", user);
            return "superadmin/user-details";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Utilisateur introuvable");
            return "redirect:/superadmin/users";
        }
    }

    // ==================== MODIFIER UN UTILISATEUR (REFACTORISÉ) ====================

    /**
     * ✅ REFACTORISÉ : Utilise maintenant un DTO
     * Affiche le formulaire de modification
     */
    @GetMapping("/users/{id}/edit")
    public String editUserForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Utilisateur user = utilisateurService.findById(id);

            // Empêcher la modification d'un autre SUPERADMIN
            if (user.getRole() == Role.SUPERADMIN) {
                redirectAttributes.addFlashAttribute("error",
                        "Impossible de modifier un autre SUPERADMIN");
                return "redirect:/superadmin/users";
            }

            // ✅ Créer un DTO à partir de l'utilisateur existant
            UpdateUtilisateurByAdminDto dto = new UpdateUtilisateurByAdminDto();
            dto.setNom(user.getNom());
            dto.setPrenom(user.getPrenom());
            dto.setEmail(user.getEmail());
            dto.setRole(user.getRole());
            // Ajoutez les autres champs si disponibles
            // dto.setTelephone(user.getTelephone());
            // dto.setAdresse(user.getAdresse());
            // dto.setDepartement(user.getDepartement());

            model.addAttribute("utilisateur", dto);
            model.addAttribute("userId", id); // Pour le formulaire
            model.addAttribute("roles", new Role[]{Role.AGENT, Role.ADMIN}); // Pas de SUPERADMIN
            return "superadmin/edit-user";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Utilisateur introuvable");
            return "redirect:/superadmin/users";
        }
    }

    /**
     * ✅ REFACTORISÉ : Utilise maintenant un DTO avec validation
     * Traite la soumission du formulaire de modification
     */
    @PostMapping("/users/{id}/edit")
    public String editUser(@PathVariable Long id,
                           @Valid @ModelAttribute("utilisateur") UpdateUtilisateurByAdminDto dto,
                           BindingResult result,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            // En cas d'erreur, conserver l'ID pour le formulaire
            model.addAttribute("userId", id);
            model.addAttribute("roles", new Role[]{Role.AGENT, Role.ADMIN});
            return "superadmin/edit-user";
        }

        try {
            // Mettre à jour via le service (qui utilise le mapper)
            utilisateurService.updateUserByAdmin(id, dto);

            redirectAttributes.addFlashAttribute("success",
                    "Utilisateur modifié avec succès");
            return "redirect:/superadmin/users/" + id;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Erreur lors de la modification : " + e.getMessage());
            return "redirect:/superadmin/users/" + id + "/edit";
        }
    }

    // ==================== ACTIVER/DÉSACTIVER UN UTILISATEUR ====================

    /**
     * Active ou désactive un utilisateur
     */
    @PostMapping("/users/{id}/toggle-status")
    public String toggleUserStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Utilisateur user = utilisateurService.findById(id);

            // Empêcher la désactivation d'un SUPERADMIN
            if (user.getRole() == Role.SUPERADMIN) {
                redirectAttributes.addFlashAttribute("error",
                        "Impossible de modifier le statut d'un SUPERADMIN");
                return "redirect:/superadmin/users";
            }

            utilisateurService.toggleUserStatus(id);

            String status = user.isEmailVerifie() ? "désactivé" : "activé";
            redirectAttributes.addFlashAttribute("success",
                    "Utilisateur " + status + " avec succès");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Erreur lors de la modification du statut");
        }

        return "redirect:/superadmin/users";
    }

    // ==================== SUPPRIMER UN UTILISATEUR ====================

    /**
     * Supprime définitivement un utilisateur
     */
    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Utilisateur user = utilisateurService.findById(id);

            // Empêcher la suppression d'un SUPERADMIN
            if (user.getRole() == Role.SUPERADMIN) {
                redirectAttributes.addFlashAttribute("error",
                        "Impossible de supprimer un SUPERADMIN");
                return "redirect:/superadmin/users";
            }

            utilisateurService.deleteUser(id);
            redirectAttributes.addFlashAttribute("success",
                    "Utilisateur supprimé avec succès");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Erreur lors de la suppression : " + e.getMessage());
        }

        return "redirect:/superadmin/users";
    }

    // ==================== RÉINITIALISER LE MOT DE PASSE ====================

    /**
     * Réinitialise le mot de passe d'un utilisateur
     * ⚠️ Le nouveau mot de passe doit respecter les critères de sécurité
     */
    @PostMapping("/users/{id}/reset-password")
    public String resetPassword(@PathVariable Long id,
                                @RequestParam String newPassword,
                                RedirectAttributes redirectAttributes) {
        try {
            Utilisateur user = utilisateurService.findById(id);

            // Empêcher la modification d'un SUPERADMIN
            if (user.getRole() == Role.SUPERADMIN) {
                redirectAttributes.addFlashAttribute("error",
                        "Impossible de modifier le mot de passe d'un SUPERADMIN");
                return "redirect:/superadmin/users/" + id;
            }

            utilisateurService.resetPasswordByAdmin(id, newPassword);
            redirectAttributes.addFlashAttribute("success",
                    "Mot de passe réinitialisé avec succès");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Erreur lors de la réinitialisation : " + e.getMessage());
        }

        return "redirect:/superadmin/users/" + id;
    }
}