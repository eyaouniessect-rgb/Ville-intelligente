package com.ville.gestionincidents.service.notification;

import com.ville.gestionincidents.dto.notification.PreferenceNotificationDTO;
import com.ville.gestionincidents.entity.PreferenceNotification;
import com.ville.gestionincidents.entity.Utilisateur;
import com.ville.gestionincidents.mapper.PreferenceNotificationMapper;
import com.ville.gestionincidents.repository.PreferenceNotificationRepository;
import com.ville.gestionincidents.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PreferenceNotificationServiceImpl implements PreferenceNotificationService {

    private final PreferenceNotificationRepository repository;
    private final UtilisateurRepository utilisateurRepository;
    private final PreferenceNotificationMapper mapper;

    /**
     * Retourne les préférences existantes (peut être null)
     */
    @Override
    @Transactional(readOnly = true)
    public PreferenceNotificationDTO getByUtilisateurId(Long utilisateurId) {
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + utilisateurId));

        PreferenceNotification entity = repository.findByUtilisateur(utilisateur);
        return mapper.toDTO(entity);
    }

    /**
     * Méthode CLÉ du système
     *
     * 👉 Si l'utilisateur n'a pas encore de préférences :
     *    - on les crée automatiquement avec les valeurs par défaut
     *    - emailActif = true
     *    - emailChangementStatut = true
     *    - pushActif = false
     *    - on les sauvegarde en base
     *
     * 👉 Sinon :
     *    - on les retourne simplement
     */
    @Override
    public PreferenceNotificationDTO getOrCreate(Long utilisateurId) {
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + utilisateurId));

        // Recherche des préférences existantes
        PreferenceNotification pref = repository.findByUtilisateur(utilisateur);

        // Si aucune préférence n'existe encore
        if (pref == null) {
            pref = new PreferenceNotification();
            pref.setUtilisateur(utilisateur);

            // Les valeurs par défaut sont déjà définies dans l'entité :
            // emailActif = true
            // emailChangementStatut = true
            // pushActif = false

            pref = repository.save(pref); // sauvegarde en base
        }

        return mapper.toDTO(pref);
    }

    /**
     * Met à jour les préférences après modification par l'utilisateur
     */
    @Override
    public PreferenceNotificationDTO updatePreferences(PreferenceNotificationDTO dto) {
        if (dto.getId() == null) {
            throw new IllegalArgumentException("L'ID de la préférence est requis pour la mise à jour");
        }

        // Récupérer l'entité existante
        PreferenceNotification entity = repository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Préférence non trouvée avec l'ID: " + dto.getId()));

        // Mettre à jour avec les nouvelles valeurs
        mapper.updateEntity(dto, entity);

        // Sauvegarder
        entity = repository.save(entity);

        return mapper.toDTO(entity);
    }
}