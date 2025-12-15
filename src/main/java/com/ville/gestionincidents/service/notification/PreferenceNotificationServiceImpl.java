package com.ville.gestionincidents.service.notification;

import com.ville.gestionincidents.entity.PreferenceNotification;
import com.ville.gestionincidents.entity.Utilisateur;
import com.ville.gestionincidents.repository.PreferenceNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service // Indique que cette classe contient de la logique métier
@RequiredArgsConstructor // Lombok : injection automatique du repository
public class PreferenceNotificationServiceImpl
        implements PreferenceNotificationService {

    // Accès à la base de données
    private final PreferenceNotificationRepository repository;

    /**
     * Retourne les préférences existantes (peut être null)
     */
    @Override
    public PreferenceNotification getByUtilisateur(Utilisateur utilisateur) {
        return repository.findByUtilisateur(utilisateur);
    }

    /**
     * Méthode CLÉ du système
     *
     * 👉 Si l’utilisateur n’a pas encore de préférences :
     *    - on les crée automatiquement
     *    - on les sauvegarde en base
     *
     * 👉 Sinon :
     *    - on les retourne simplement
     */
    @Override
    public PreferenceNotification getOrCreate(Utilisateur utilisateur) {

        // Recherche des préférences existantes
        PreferenceNotification pref =
                repository.findByUtilisateur(utilisateur);

        // Si aucune préférence n’existe encore
        if (pref == null) {

            pref = new PreferenceNotification();
            pref.setUtilisateur(utilisateur);

            // Valeurs par défaut (logique métier)
            // emailActif = true
            // emailChangementStatut = true
            // pushActif = false

            repository.save(pref); // sauvegarde en base
        }

        return pref;
    }

    /**
     * Met à jour les préférences après modification par l’utilisateur
     */
    @Override
    public void updatePreferences(PreferenceNotification preferences) {
        repository.save(preferences);
    }
}
