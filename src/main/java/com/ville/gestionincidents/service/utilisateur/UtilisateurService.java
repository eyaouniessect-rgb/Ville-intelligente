package com.ville.gestionincidents.service.utilisateur;

import com.ville.gestionincidents.dto.auth.RegisterDto;
import com.ville.gestionincidents.entity.Utilisateur;
import com.ville.gestionincidents.enumeration.Role;

import java.util.List;

public interface UtilisateurService {

    // ==================== INSCRIPTION ET VÉRIFICATION ====================

    /**
     * Inscription d'un nouveau citoyen avec vérification d'email
     */
    boolean register(RegisterDto dto);

    /**
     * Vérification de l'email avec le token
     */
    boolean verifyEmail(String token);

    /**
     * Recherche d'un utilisateur par email
     */
    Utilisateur findByEmail(String email);

    // ==================== GESTION PAR SUPERADMIN ====================

    /**
     * Création d'un utilisateur par un admin/superadmin
     * (pas besoin de vérification email)
     */
    Utilisateur createUserByAdmin(Utilisateur utilisateur);

    /**
     * Modification d'un utilisateur par un admin
     */
    Utilisateur updateUserByAdmin(Long id, Utilisateur utilisateur);

    /**
     * Recherche d'un utilisateur par ID
     */
    Utilisateur findById(Long id);

    /**
     * Liste de tous les utilisateurs sauf les SUPERADMIN
     */
    List<Utilisateur> findAllExceptSuperAdmin();

    /**
     * Liste de tous les utilisateurs
     */
    List<Utilisateur> findAll();

    /**
     * Liste des utilisateurs par rôle
     */
    List<Utilisateur> findByRole(Role role);

    /**
     * Suppression d'un utilisateur
     */
    void deleteUser(Long id);

    /**
     * Activer/Désactiver un utilisateur
     */
    void toggleUserStatus(Long id);

    /**
     * Réinitialiser le mot de passe d'un utilisateur
     */
    void resetPasswordByAdmin(Long id, String newPassword);

    // ==================== STATISTIQUES ====================

    /**
     * Compte total des utilisateurs
     */
    long countAllUsers();

    /**
     * Compte des utilisateurs par rôle
     */
    long countByRole(Role role);

    /**
     * Liste des N utilisateurs les plus récents
     */
    List<Utilisateur> findRecentUsers(int limit);

}