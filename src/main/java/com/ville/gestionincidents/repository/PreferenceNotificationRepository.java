package com.ville.gestionincidents.repository;

import com.ville.gestionincidents.entity.PreferenceNotification;
import com.ville.gestionincidents.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PreferenceNotificationRepository
        extends JpaRepository<PreferenceNotification, Long> {

    PreferenceNotification findByUtilisateur(Utilisateur utilisateur);
}
