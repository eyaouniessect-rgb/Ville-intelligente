package com.ville.gestionincidents.service.notification;

import com.ville.gestionincidents.entity.PreferenceNotification;
import com.ville.gestionincidents.entity.Utilisateur;

/**
 * Interface métier des préférences de notification
 * Elle définit CE QU’ON PEUT FAIRE, pas COMMENT
 */
public interface PreferenceNotificationService {

    /**
     * Récupérer les préférences d’un utilisateur
     * (peut retourner null)
     */
    PreferenceNotification getByUtilisateur(Utilisateur utilisateur);

    /**
     * Récupérer les préférences OU les créer automatiquement
     * si elles n’existent pas encore
     */
    PreferenceNotification getOrCreate(Utilisateur utilisateur);

    /**
     * Mettre à jour les préférences de l’utilisateur
     */
    void updatePreferences(PreferenceNotification preferences);
}
