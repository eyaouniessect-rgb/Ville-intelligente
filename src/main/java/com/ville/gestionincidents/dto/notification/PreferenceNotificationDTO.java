package com.ville.gestionincidents.dto.notification;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour les préférences de notification
 * Utilisé pour l'échange de données entre les couches
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreferenceNotificationDTO {

    private Long id;

    private Long utilisateurId;

    private boolean emailActif;

    private boolean emailChangementStatut;

    private boolean pushActif;
}
