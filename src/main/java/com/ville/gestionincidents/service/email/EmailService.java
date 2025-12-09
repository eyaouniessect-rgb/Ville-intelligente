package com.ville.gestionincidents.service.email;

public interface EmailService {

    /**
     * Envoie un email de vérification à l'utilisateur lors de l'inscription
     * @param to Email du destinataire
     * @param token Token de vérification unique
     */
    void sendVerificationEmail(String to, String token);

    /**
     * Envoie une notification de mise à jour d'incident
     * @param to Email du destinataire
     * @param incidentId ID de l'incident
     * @param nouveauStatut Nouveau statut de l'incident
     */
    void sendIncidentUpdateEmail(String to, Long incidentId, String nouveauStatut);

    /**
     * Envoie un email de réinitialisation de mot de passe
     * @param to Email du destinataire
     * @param token Token de réinitialisation
     */
    void sendPasswordResetEmail(String to, String token);
}