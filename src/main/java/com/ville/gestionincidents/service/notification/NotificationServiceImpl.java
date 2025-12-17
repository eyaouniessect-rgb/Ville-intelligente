package com.ville.gestionincidents.service.notification;

import com.ville.gestionincidents.entity.Incident;
import com.ville.gestionincidents.entity.Notification;
import com.ville.gestionincidents.entity.Utilisateur;
import com.ville.gestionincidents.enumeration.TypeNotification;
import com.ville.gestionincidents.repository.NotificationRepository;
import com.ville.gestionincidents.repository.PreferenceNotificationRepository;
import com.ville.gestionincidents.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.ville.gestionincidents.entity.PreferenceNotification;
import com.ville.gestionincidents.service.email.EmailService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PreferenceNotificationRepository preferenceNotificationRepository;
    private final EmailService emailService;
    private final WebSocketNotificationService webSocketNotificationService;

    @Override
    public void creerNotification(
            String emailUtilisateur,
            TypeNotification type,
            String message,
            Incident incident
    ) {
        try {
            Utilisateur utilisateur = utilisateurRepository
                    .findByEmail(emailUtilisateur)
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable: " + emailUtilisateur));

            log.info("🔔 Création de notification pour utilisateur ID: {}, Type: {}", utilisateur.getId(), type);

            // 🔹 1) Toujours enregistrer une notification interne
            Notification notification = new Notification();
            notification.setUtilisateur(utilisateur);
            notification.setType(type);
            notification.setMessage(message);
            notification.setIncident(incident);
            notification.setDateEnvoi(LocalDateTime.now());
            notification.setLu(false);

            notificationRepository.save(notification);
            log.info("✅ Notification sauvegardée en BDD");

            // 🔹 2) Charger les préférences utilisateur
            PreferenceNotification pref = preferenceNotificationRepository.findByUtilisateur(utilisateur);

            if (pref == null) {
                log.warn("⚠️ Aucune préférence trouvée pour l'utilisateur {}", utilisateur.getId());
                return;
            }

            log.info("📋 Préférences: emailActif={}, pushActif={}", pref.isEmailActif(), pref.isPushActif());

            // 🔹 3) Envoi email si activé
            if (pref.isEmailActif()) {
                envoyerEmailSelonType(type, pref, utilisateur.getEmail(), message);
            }

            // 🔹 4) Envoi push WebSocket si activé
            if (pref.isPushActif()) {
                envoyerPushSelonType(type, pref, utilisateur.getId(), message);
            }

        } catch (Exception e) {
            log.error("❌ Erreur lors de la création de notification: {}", e.getMessage(), e);
        }
    }

    /**
     * Envoie un email selon le type de notification et les préférences
     */
    private void envoyerEmailSelonType(TypeNotification type, PreferenceNotification pref,
                                       String email, String message) {
        try {
            switch (type) {
                case CREATION_INCIDENT:
                    emailService.sendSimpleEmail(email, "Incident créé", message);
                    log.info("📧 Email CREATION_INCIDENT envoyé");
                    break;

                case CHANGEMENT_STATUT:
                    if (pref.isEmailChangementStatut()) {
                        emailService.sendSimpleEmail(email, "Mise à jour du l'incident", message);
                        log.info("📧 Email CHANGEMENT_STATUT envoyé");
                    }
                    break;

                case ASSIGNATION:
                    emailService.sendSimpleEmail(email, "Assignation d'incident", message);
                    log.info("📧 Email ASSIGNATION envoyé");
                    break;
            }
        } catch (Exception e) {
            log.error("❌ Erreur lors de l'envoi d'email: {}", e.getMessage());
        }
    }

    /**
     * Envoie une notification push WebSocket selon le type et les préférences
     */
    private void envoyerPushSelonType(TypeNotification type, PreferenceNotification pref,
                                      Long utilisateurId, String message) {
        try {
            boolean shouldSend = false;

            switch (type) {
                case CREATION_INCIDENT:
                    // Toujours envoyer pour création d'incident si push est actif
                    shouldSend = true;
                    break;

                case CHANGEMENT_STATUT:

                    shouldSend = pref.isEmailChangementStatut();
                    break;

                case ASSIGNATION:
                    // Toujours envoyer pour assignation si push est actif
                    shouldSend = true;
                    break;
            }

            if (shouldSend) {
                log.info("🚀 Envoi notification WebSocket - User ID: {}, Type: {}", utilisateurId, type);
                webSocketNotificationService.sendNotification(utilisateurId, message, type);
                log.info("✅ Notification WebSocket envoyée avec succès");
            } else {
                log.info("⏭️ Notification WebSocket ignorée selon préférences - Type: {}", type);
            }

        } catch (Exception e) {
            log.error("❌ Erreur lors de l'envoi de notification WebSocket: {}", e.getMessage(), e);
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