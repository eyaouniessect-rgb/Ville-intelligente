package com.ville.gestionincidents.controller.superadmin;

import com.ville.gestionincidents.entity.Utilisateur;
import com.ville.gestionincidents.enumeration.Role;
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

@Controller
@RequestMapping("/superadmin")
@PreAuthorize("hasRole('SUPERADMIN')")
public class SuperAdminController {

    @Autowired
    private UtilisateurService utilisateurService;

    // ==================== DASHBOARD ====================
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalUsers", utilisateurService.countAllUsers());
        model.addAttribute("totalAdmins", utilisateurService.countByRole(Role.ADMIN));
        model.addAttribute("totalAgents", utilisateurService.countByRole(Role.AGENT));
        model.addAttribute("totalCitoyens", utilisateurService.countByRole(Role.CITOYEN));

        // Utilisateurs récents
        model.addAttribute("recentUsers", utilisateurService.findRecentUsers(5));

        return "superadmin/dashboard";
    }

    // ==================== LISTE DES UTILISATEURS ====================
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

    // ==================== CRÉER UN ADMINISTRATEUR ====================
    @GetMapping("/create-admin")
    public String createAdminForm(Model model) {
        model.addAttribute("utilisateur", new Utilisateur());
        return "superadmin/create-admin";
    }

    @PostMapping("/create-admin")
    public String createAdmin(@Valid @ModelAttribute Utilisateur utilisateur,
                              BindingResult result,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "superadmin/create-admin";
        }

        try {
            utilisateur.setRole(Role.ADMIN);
            utilisateur.setEmailVerifie(true); // Les admins sont pré-vérifiés
            utilisateurService.createUserByAdmin(utilisateur);

            redirectAttributes.addFlashAttribute("success",
                    "Administrateur '" + utilisateur.getEmail() + "' créé avec succès");
            return "redirect:/superadmin/users?role=ADMIN";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Erreur lors de la création : " + e.getMessage());
            return "redirect:/superadmin/create-admin";
        }
    }

    // ==================== CRÉER UN AGENT ====================
    @GetMapping("/create-agent")
    public String createAgentForm(Model model) {
        model.addAttribute("utilisateur", new Utilisateur());
        // Vous pouvez ajouter la liste des départements ici si nécessaire
        return "superadmin/create-agent";
    }

    @PostMapping("/create-agent")
    public String createAgent(@Valid @ModelAttribute Utilisateur utilisateur,
                              BindingResult result,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "superadmin/create-agent";
        }

        try {
            utilisateur.setRole(Role.AGENT);
            utilisateur.setEmailVerifie(true); // Les agents sont pré-vérifiés
            utilisateurService.createUserByAdmin(utilisateur);

            redirectAttributes.addFlashAttribute("success",
                    "Agent '" + utilisateur.getEmail() + "' créé avec succès");
            return "redirect:/superadmin/users?role=AGENT";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Erreur lors de la création : " + e.getMessage());
            return "redirect:/superadmin/create-agent";
        }
    }

    // ==================== VOIR DÉTAILS D'UN UTILISATEUR ====================
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

    // ==================== MODIFIER UN UTILISATEUR ====================
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

            model.addAttribute("user", user);
            model.addAttribute("roles", new Role[]{Role.AGENT, Role.ADMIN}); // Pas de SUPERADMIN
            return "superadmin/edit-user";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Utilisateur introuvable");
            return "redirect:/superadmin/users";
        }
    }

    @PostMapping("/users/{id}/edit")
    public String editUser(@PathVariable Long id,
                           @Valid @ModelAttribute ("user")Utilisateur utilisateur,
                           BindingResult result,
                           RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "superadmin/edit-user";
        }

        try {
            utilisateurService.updateUserByAdmin(id, utilisateur);
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

