package com.ville.gestionincidents.service.notification;

import com.ville.gestionincidents.entity.Incident;
import com.ville.gestionincidents.entity.Notification;
import com.ville.gestionincidents.entity.Utilisateur;
import com.ville.gestionincidents.enumeration.TypeNotification;
import com.ville.gestionincidents.repository.NotificationRepository;
import com.ville.gestionincidents.repository.PreferenceNotificationRepository;
import com.ville.gestionincidents.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.ville.gestionincidents.entity.PreferenceNotification;
import com.ville.gestionincidents.service.email.EmailService;



import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PreferenceNotificationRepository preferenceNotificationRepository;
    private final EmailService emailService;


    @Override
    public void creerNotification(
            String emailUtilisateur,
            TypeNotification type,
            String message,
            Incident incident
    ){
        Utilisateur utilisateur = utilisateurRepository
                .findByEmail(emailUtilisateur)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        // 🔹 1) Toujours enregistrer une notification interne
        Notification notification = new Notification();
        notification.setUtilisateur(utilisateur);
        notification.setType(type);
        notification.setMessage(message);
        notification.setIncident(incident);
        notification.setDateEnvoi(LocalDateTime.now());
        notification.setLu(false);

        notificationRepository.save(notification);

        // 🔹 2) Charger les préférences utilisateur
        PreferenceNotification pref =
                preferenceNotificationRepository.findByUtilisateur(utilisateur);

        if (pref == null) return; // (ne devrait jamais arriver)

        // 🔹 3) Envoi email si activé
        if (pref.isEmailActif()) {
            switch (type) {
                case CREATION_INCIDENT:
                    emailService.sendSimpleEmail(
                            utilisateur.getEmail(),
                            "Incident créé",
                            message
                    );
                    break;

                case CHANGEMENT_STATUT:
                    if (pref.isEmailChangementStatut()) {
                        emailService.sendSimpleEmail(
                                utilisateur.getEmail(),
                                "Mise à jour de votre incident",
                                message
                        );
                    }
                    break;
            }
        }

        // 🔹 4) Envoi push si activé (plus tard)
        if (pref.isPushActif()) {
            // websocketService.sendNotification(utilisateur, message);
        }
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
