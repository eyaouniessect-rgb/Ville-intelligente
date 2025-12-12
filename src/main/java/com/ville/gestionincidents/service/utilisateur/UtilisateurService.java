package com.ville.gestionincidents.service.utilisateur;

import com.ville.gestionincidents.dto.auth.RegisterDto;
import com.ville.gestionincidents.dto.utilisateur.citoyen.ChangePasswordDto;
import com.ville.gestionincidents.dto.utilisateur.citoyen.CitoyenProfilDto;
import com.ville.gestionincidents.dto.utilisateur.citoyen.CitoyenUpdateProfilDto;
import com.ville.gestionincidents.dto.utilisateur.superAdmin.CreateUtilisateurByAdminDto;
import com.ville.gestionincidents.dto.utilisateur.superAdmin.UpdateUtilisateurByAdminDto;
import com.ville.gestionincidents.entity.Utilisateur;
import com.ville.gestionincidents.enumeration.Role;

import java.util.List;

/**
 * Interface du service Utilisateur
 * Définit toutes les opérations possibles sur les utilisateurs
 *
 * ✅ REFACTORISÉ : Utilise des DTOs au lieu d'entités pour create/update
 */
public interface UtilisateurService {

    // ==================== INSCRIPTION ET VÉRIFICATION (CITOYENS) ====================

    /**
     * Inscrit un nouveau citoyen
     * @param dto Données d'inscription (nom, prénom, email, mot de passe)
     * @return true si l'inscription a réussi, false sinon
     */
    boolean register(RegisterDto dto);

    /**
     * Vérifie l'email d'un utilisateur via le token envoyé par email
     * @param token Token de vérification unique
     * @return true si la vérification a réussi, false si token invalide/expiré
     */
    boolean verifyEmail(String token);
    //===========================profil citoyen avec dto =============
    CitoyenProfilDto getProfilCitoyen(String email);
    void updateProfilCitoyen(String email, CitoyenUpdateProfilDto dto);


    // ==================== GESTION PAR SUPERADMIN (AVEC DTO) ====================

    /**
     * ✅ REFACTORISÉ : Crée un utilisateur (ADMIN ou AGENT) via DTO
     *
     * @param dto Les données du formulaire (nom, prénom, email, mot de passe, etc.)
     * @param role Le rôle à attribuer (ADMIN ou AGENT uniquement)
     * @return L'utilisateur créé
     * @throws RuntimeException si l'email existe déjà ou si le rôle est SUPERADMIN
     */
    Utilisateur createUserByAdmin(CreateUtilisateurByAdminDto dto, Role role);

    /**
     * ✅ REFACTORISÉ : Met à jour un utilisateur via DTO
     *
     * Ne modifie PAS le mot de passe (géré via resetPasswordByAdmin)
     *
     * @param id L'ID de l'utilisateur à modifier
     * @param dto Les nouvelles données (nom, prénom, email, rôle)
     * @return L'utilisateur modifié
     * @throws RuntimeException si l'utilisateur n'existe pas ou est SUPERADMIN
     */
    Utilisateur updateUserByAdmin(Long id, UpdateUtilisateurByAdminDto dto);

    /**
     * Réinitialise le mot de passe d'un utilisateur
     *
     * @param id L'ID de l'utilisateur
     * @param newPassword Le nouveau mot de passe (doit respecter les critères)
     * @throws RuntimeException si l'utilisateur est SUPERADMIN ou mot de passe invalide
     */
    void resetPasswordByAdmin(Long id, String newPassword);

    /**
     * Active ou désactive un utilisateur (inverse le statut emailVerifie)
     *
     * @param id L'ID de l'utilisateur
     * @throws RuntimeException si l'utilisateur est SUPERADMIN
     */
    void toggleUserStatus(Long id);

    /**
     * Supprime définitivement un utilisateur
     *
     * @param id L'ID de l'utilisateur à supprimer
     * @throws RuntimeException si l'utilisateur est SUPERADMIN
     */
    void deleteUser(Long id);

    // ==================== RECHERCHE D'UTILISATEURS ====================

    /**
     * Trouve un utilisateur par son ID
     * @throws RuntimeException si l'utilisateur n'existe pas
     */
    Utilisateur findById(Long id);

    /**
     * Trouve un utilisateur par son email
     * @throws RuntimeException si l'utilisateur n'existe pas
     */
    Utilisateur findByEmail(String email);

    /**
     * Récupère tous les utilisateurs
     */
    List<Utilisateur> findAll();

    /**
     * Récupère tous les utilisateurs SAUF les SUPERADMIN
     * Utile pour l'affichage dans le dashboard SuperAdmin
     */
    List<Utilisateur> findAllExceptSuperAdmin();

    /**
     * Récupère tous les utilisateurs d'un rôle spécifique
     * @param role Le rôle à filtrer (ADMIN, AGENT, CITOYEN, SUPERADMIN)
     */
    List<Utilisateur> findByRole(Role role);

    /**
     * Récupère les utilisateurs les plus récents
     * @param limit Nombre maximum d'utilisateurs à retourner
     */
    List<Utilisateur> findRecentUsers(int limit);

    // ==================== STATISTIQUES ====================

    /**
     * Compte le nombre total d'utilisateurs (tous rôles confondus)
     */
    long countAllUsers();

    /**
     * Compte le nombre d'utilisateurs pour un rôle donné
     * @param role Le rôle à compter
     */
    long countByRole(Role role);
    void changePasswordCitoyen(Long userId, ChangePasswordDto dto);

}