package com.ville.gestionincidents.service.email;

import com.ville.gestionincidents.enumeration.Role;

public interface EmailService {

    // ==================== MÉTHODES EXISTANTES ====================

    /**
     * Envoie un email de vérification pour l'inscription
     */
    void sendVerificationEmail(String to, String token);

    /**
     * Envoie une notification de mise à jour d'incident
     */
    void sendIncidentUpdateEmail(String to, Long incidentId, String nouveauStatut);

    /**
     * Envoie un email de réinitialisation de mot de passe
     */
    void sendPasswordResetEmail(String to, String token);

    // ==================== NOUVELLES MÉTHODES POUR SUPERADMIN ====================

    /**
     * ✅ AJOUTER : Envoie un email de bienvenue pour les utilisateurs créés par admin
     */
    void sendWelcomeEmail(String to, String nom, Role role);

    /**
     * ✅ AJOUTER : Envoie une notification après réinitialisation du mot de passe par admin
     */
    void sendPasswordResetNotification(String to);
}