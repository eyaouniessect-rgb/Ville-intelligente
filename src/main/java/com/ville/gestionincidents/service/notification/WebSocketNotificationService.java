package com.ville.gestionincidents.service.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ville.gestionincidents.enumeration.TypeNotification;
import lombok.Data;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
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
        try {
            NotificationMessage notificationMessage = new NotificationMessage();
            notificationMessage.setMessage(message);
            notificationMessage.setType(typeNotification.name()); // Envoie le nom de l'enum (CREATION_INCIDENT, etc.)
            notificationMessage.setTimestamp(System.currentTimeMillis());

            String jsonMessage = objectMapper.writeValueAsString(notificationMessage);
            messagingTemplate.convertAndSend("/topic/notifications/" + utilisateurId, jsonMessage);
        } catch (JsonProcessingException e) {
            // Fallback to simple message if JSON serialization fails
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
