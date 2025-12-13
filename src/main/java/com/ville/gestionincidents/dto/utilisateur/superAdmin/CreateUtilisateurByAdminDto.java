package com.ville.gestionincidents.dto.utilisateur.superAdmin;

import lombok.Data;
import javax.validation.constraints.*;

/**
 * DTO pour la création d'utilisateurs (ADMIN/AGENT) par le SuperAdmin
 * Contient toutes les validations nécessaires pour garantir la qualité des données
 */
@Data
public class CreateUtilisateurByAdminDto {

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

    // ⚠️ Validation côté service pour les 12 caractères + complexité
    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 12, message = "Le mot de passe doit contenir au moins 12 caractères")
    private String motDePasse;

    // ==================== CHAMPS OPTIONNELS ====================

    // Ces champs peuvent être ajoutés si vous avez ces colonnes dans votre entité
    private String telephone;
    private String adresse;

    // ==================== CHAMP SPÉCIFIQUE AGENT ====================

    @NotNull(message = "Le département est obligatoire pour un administrateur")
    private Long departementId;

}