package com.ville.gestionincidents.mapper;



import com.ville.gestionincidents.dto.notification.PreferenceNotificationDTO;
import com.ville.gestionincidents.entity.PreferenceNotification;
import com.ville.gestionincidents.entity.Utilisateur;
import org.springframework.stereotype.Component;

/**
 * Mapper pour convertir entre Entity et DTO
 */
@Component
public class PreferenceNotificationMapper {

    /**
     * Convertit une Entity en DTO
     */
    public PreferenceNotificationDTO toDTO(PreferenceNotification entity) {
        if (entity == null) {
            return null;
        }

        return PreferenceNotificationDTO.builder()
                .id(entity.getId())
                .utilisateurId(entity.getUtilisateur() != null ?
                        entity.getUtilisateur().getId() : null)
                .emailActif(entity.isEmailActif())
                .emailChangementStatut(entity.isEmailChangementStatut())
                .pushActif(entity.isPushActif())
                .build();
    }

    /**
     * Convertit un DTO en Entity (pour création)
     */
    public PreferenceNotification toEntity(PreferenceNotificationDTO dto, Utilisateur utilisateur) {
        if (dto == null) {
            return null;
        }

        PreferenceNotification entity = new PreferenceNotification();
        entity.setId(dto.getId());
        entity.setUtilisateur(utilisateur);
        entity.setEmailActif(dto.isEmailActif());
        entity.setEmailChangementStatut(dto.isEmailChangementStatut());
        entity.setPushActif(dto.isPushActif());

        return entity;
    }

    /**
     * Met à jour une Entity existante avec les données d'un DTO
     */
    public void updateEntity(PreferenceNotificationDTO dto, PreferenceNotification entity) {
        if (dto == null || entity == null) {
            return;
        }

        entity.setEmailActif(dto.isEmailActif());
        entity.setEmailChangementStatut(dto.isEmailChangementStatut());
        entity.setPushActif(dto.isPushActif());
    }
}