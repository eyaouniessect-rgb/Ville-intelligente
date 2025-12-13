package com.ville.gestionincidents.service.notification;

import com.ville.gestionincidents.entity.Incident;
import com.ville.gestionincidents.entity.Notification;
import com.ville.gestionincidents.enumeration.TypeNotification;

import java.util.List;

public interface NotificationService {

    void creerNotification(
            String emailUtilisateur,
            TypeNotification type,
            String message,
            Incident incident
    );

    List<Notification> getNotificationsByEmail(String email);

    long countNotificationsNonLues(String email);

    void marquerCommeLue(Long notificationId);
}
