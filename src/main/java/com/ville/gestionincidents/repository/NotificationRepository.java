package com.ville.gestionincidents.repository;

import com.ville.gestionincidents.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUtilisateur_EmailOrderByDateEnvoiDesc(String email);

    long countByUtilisateur_EmailAndLuFalse(String email);
}
