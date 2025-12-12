package com.ville.gestionincidents.repository;

import com.ville.gestionincidents.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Récupérer toutes les notifications d'un utilisateur via son email
    List<Notification> findByUtilisateurEmail(String email);
}
