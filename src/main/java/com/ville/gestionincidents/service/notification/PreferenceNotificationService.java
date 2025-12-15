package com.ville.gestionincidents.service.notification;

import com.ville.gestionincidents.dto.notification.PreferenceNotificationDTO;

/**
 * Interface métier des préférences de notification
 * Elle définit CE QU'ON PEUT FAIRE, pas COMMENT
 */
public interface PreferenceNotificationService {

    /**
     * Récupérer les préférences d'un utilisateur par son ID
     * (peut retourner null)
     */
    PreferenceNotificationDTO getByUtilisateurId(Long utilisateurId);

    /**
     * Récupérer les préférences OU les créer automatiquement
     * si elles n'existent pas encore
     */
    PreferenceNotificationDTO getOrCreate(Long utilisateurId);

    /**
     * Mettre à jour les préférences de l'utilisateur
     */
    PreferenceNotificationDTO updatePreferences(PreferenceNotificationDTO preferences);
}