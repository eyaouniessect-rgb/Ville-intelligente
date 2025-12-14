package com.ville.gestionincidents.service.notification;

import com.ville.gestionincidents.entity.Incident;
import com.ville.gestionincidents.entity.Notification;
import com.ville.gestionincidents.entity.Utilisateur;
import com.ville.gestionincidents.enumeration.TypeNotification;
import com.ville.gestionincidents.repository.NotificationRepository;
import com.ville.gestionincidents.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UtilisateurRepository utilisateurRepository;

    @Override
    public void creerNotification(
            String emailUtilisateur,
            TypeNotification type,
            String message,
            Incident incident
    )
    {
        Utilisateur utilisateur = utilisateurRepository
                .findByEmail(emailUtilisateur)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        Notification notification = new Notification();
        notification.setUtilisateur(utilisateur);
        notification.setType(type);
        notification.setMessage(message);
        notification.setIncident(incident);
        notification.setDateEnvoi(LocalDateTime.now());
        notification.setLu(false);

        notificationRepository.save(notification);
    }

    @Override
    public List<Notification> getNotificationsByEmail(String email) {
        return notificationRepository
                .findByUtilisateur_EmailOrderByDateEnvoiDesc(email);
    }

    @Override
    public long countNotificationsNonLues(String email) {
        return notificationRepository
                .countByUtilisateur_EmailAndLuFalse(email);
    }

    @Override
    public void marquerCommeLue(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification introuvable"));

        notification.setLu(true);
        notificationRepository.save(notification);
    }
}
