package com.ville.gestionincidents.service.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ville.gestionincidents.enumeration.TypeNotification;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    public WebSocketNotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Envoie une notification à un utilisateur spécifique via WebSocket
     * Utilise le type de notification métier (CREATION_INCIDENT, CHANGEMENT_STATUT, ASSIGNATION)
     */
    public void sendNotification(Long utilisateurId, String message, TypeNotification typeNotification) {
        log.info("📤 Tentative d'envoi WebSocket - User: {}, Type: {}", utilisateurId, typeNotification);
        try {
            NotificationMessage notificationMessage = new NotificationMessage();
            notificationMessage.setMessage(message);
            notificationMessage.setType(typeNotification.name());
            notificationMessage.setTimestamp(System.currentTimeMillis());

            String jsonMessage = objectMapper.writeValueAsString(notificationMessage);
            log.info("📨 Message JSON: {}", jsonMessage);

            String destination = "/topic/notifications/" + utilisateurId;
            log.info("🎯 Destination: {}", destination);

            messagingTemplate.convertAndSend(destination, jsonMessage);
            log.info("✅ Message envoyé avec succès");
        } catch (Exception e) {
            log.error("❌ Erreur WebSocket: {}", e.getMessage(), e);
            messagingTemplate.convertAndSend("/topic/notifications/" + utilisateurId, message);
        }
    }

    @Data
    private static class NotificationMessage {
        private String message;
        private String type; // CREATION_INCIDENT, CHANGEMENT_STATUT, ASSIGNATION
        private Long timestamp;
    }
}
