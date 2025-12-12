package com.ville.gestionincidents.dto.utilisateur.superAdmin;

import com.ville.gestionincidents.enumeration.Role;
import lombok.Data;
import javax.validation.constraints.*;

/**
 * DTO pour la modification d'utilisateurs par le SuperAdmin
 * ⚠️ IMPORTANT : Ne contient PAS le mot de passe (géré séparément pour la sécurité)
 *
 * Utilisé dans :
 * - SuperAdminController.editUserForm() : Affiche le formulaire
 * - SuperAdminController.editUser() : Traite la soumission
 * - UtilisateurService.updateUserByAdmin() : Mise à jour en base
 */
@Data
public class UpdateUtilisateurByAdminDto {

    // ==================== CHAMPS OBLIGATOIRES ====================

    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 50, message = "Le nom doit contenir entre 2 et 50 caractères")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(min = 2, max = 50, message = "Le prénom doit contenir entre 2 et 50 caractères")
    private String prenom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;

    /**
     * Le rôle peut être modifié par le SuperAdmin
     * IMPORTANT : La validation du rôle (pas SUPERADMIN) se fait dans le service
     */
    @NotNull(message = "Le rôle est obligatoire")
    private Role role;

    // ==================== CHAMPS OPTIONNELS ====================

    /**
     * Numéro de téléphone (optionnel)
     * Décommentez dans le mapper si vous avez ce champ dans l'entité
     */
    private String telephone;

    /**
     * Adresse postale (optionnel)
     * Décommentez dans le mapper si vous avez ce champ dans l'entité
     */
    private String adresse;

    /**
     * Département de travail pour les agents
     * Sera ignoré automatiquement si le rôle n'est pas AGENT
     * Décommentez dans le mapper si vous avez ce champ dans l'entité
     */
    private String departement;
}