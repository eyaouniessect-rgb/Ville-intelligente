package com.ville.gestionincidents.service.incident;

import com.ville.gestionincidents.dto.incident.IncidentCreateDto;
import com.ville.gestionincidents.dto.incident.IncidentDetailsDto;
import com.ville.gestionincidents.dto.incident.IncidentListDto;
import com.ville.gestionincidents.entity.Incident;
import com.ville.gestionincidents.entity.Photo;
import com.ville.gestionincidents.entity.Utilisateur;
import com.ville.gestionincidents.enumeration.StatutIncident;
import com.ville.gestionincidents.mapper.IncidentMapper;
import com.ville.gestionincidents.repository.IncidentRepository;
import com.ville.gestionincidents.repository.PhotoRepository;
import com.ville.gestionincidents.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.ville.gestionincidents.service.notification.NotificationService;
import com.ville.gestionincidents.enumeration.TypeNotification;

import java.util.List;

/**
 * Service métier : gère la déclaration d'incident par un citoyen.
 */
@Service
@RequiredArgsConstructor
public class IncidentServiceImpl implements IncidentService {

    private final IncidentRepository incidentRepository;
    private final PhotoRepository photoRepository;
    private final IncidentMapper incidentMapper;
    private final PhotoStorageService photoStorageService;
    private final CurrentUserService currentUserService;
    private final NotificationService notificationService;


    @Override
    public void creerIncident(IncidentCreateDto dto) {

        // LOG POUR DÉBOGAGE
        System.out.println("=== CRÉATION INCIDENT ===");
        System.out.println("Description: " + dto.getDescription());
        System.out.println("Nombre de photos reçues: " +
                (dto.getPhotos() != null ? dto.getPhotos().size() : 0));

        if (dto.getPhotos() != null) {
            for (MultipartFile f : dto.getPhotos()) {
                System.out.println("  - Fichier: " + f.getOriginalFilename() +
                        " | Taille: " + f.getSize() + " bytes");
            }
        }

        // 1️⃣ Convertir le DTO en entité Incident
        Incident incident = incidentMapper.toEntity(dto);

        // 2️⃣ Associer automatiquement le citoyen connecté
        Utilisateur citoyen = currentUserService.getCurrentUser();
        incident.setCitoyen(citoyen);
        incident.setStatut(StatutIncident.SIGNALE);

        // 3️⃣ Sauvegarder l'incident
        incident = incidentRepository.save(incident); // ✅ Récupérer l'incident sauvegardé

        // 🔔 Notification : création d'incident
        notificationService.creerNotification(
                citoyen.getEmail(),
                TypeNotification.CREATION_INCIDENT,
                "Votre incident a été créé avec succès",
                incident
        );


        // 4️⃣ Gérer plusieurs photos
        if (dto.getPhotos() != null && !dto.getPhotos().isEmpty()) {

            boolean isFirst = true;

            for (MultipartFile fichier : dto.getPhotos()) {
                // ✅ Vérifier que le fichier n'est pas vide
                if (fichier == null || fichier.isEmpty()) {
                    System.out.println("  ⚠️ Fichier vide ou null, ignoré");
                    continue;
                }

                try {
                    // 4.1 Sauvegarde dans /uploads/
                    String nomFichier = photoStorageService.save(fichier);
                    System.out.println("  ✅ Photo sauvegardée: " + nomFichier);

                    // 4.2 Enregistrement BD
                    Photo photo = new Photo();
                    photo.setNomFichier(nomFichier);
                    photo.setTypeContenu(fichier.getContentType());
                    photo.setCheminStockage("uploads/" + nomFichier);
                    photo.setPrincipale(isFirst);
                    photo.setIncident(incident);

                    photoRepository.save(photo);
                    System.out.println("  ✅ Photo enregistrée en BD: ID=" + photo.getId());

                    isFirst = false;

                } catch (Exception e) {
                    System.err.println("  ❌ Erreur lors du traitement de la photo: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } else {
            System.out.println("⚠️ Aucune photo à traiter");
        }

        System.out.println("=== FIN CRÉATION INCIDENT ===");
    }
    //pour recuperer les incidents d'un utlisateur connecte
    @Override
    public List<IncidentListDto> getIncidentsForCurrentUser() {
        Utilisateur user = currentUserService.getCurrentUser();
        List<Incident> incidents = incidentRepository.findByCitoyen(user);
        return incidentMapper.toListDtos(incidents);
    }


    //recuperee les incidents d'un utlisateur connnecte par status
    @Override
    public List<IncidentListDto> getIncidentsByStatutForCurrentUser(StatutIncident statut) {
        Utilisateur user = currentUserService.getCurrentUser();
        List<Incident> incidents = incidentRepository.findByCitoyenIdAndStatut(user.getId(), statut);
        return incidentMapper.toListDtos(incidents);
    }


    @Override
    public IncidentDetailsDto getIncidentDetailsForCurrentUser(Long id) {
        Incident inc = incidentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Incident introuvable"));

        if (!inc.getCitoyen().getId().equals(currentUserService.getCurrentUserId())) {
            throw new RuntimeException("Accès non autorisé à cet incident !");
        }

        return incidentMapper.toDetailsDto(inc);
    }


    @Override
    public int countForCurrentUser() {
        String email = currentUserService.getCurrentUser().getEmail();
        return incidentRepository.countByCitoyenEmail(email);
    }

    @Override
    public int countSignaleForCurrentUser() {
        String email = currentUserService.getCurrentUser().getEmail();
        return incidentRepository.countByCitoyenEmailAndStatut(email, StatutIncident.SIGNALE);
    }

    @Override
    public int countPrisEnChargeForCurrentUser() {
        String email = currentUserService.getCurrentUser().getEmail();
        return incidentRepository.countByCitoyenEmailAndStatut(email, StatutIncident.PRIS_EN_CHARGE);
    }

    @Override
    public int countEnResolutionForCurrentUser() {
        String email = currentUserService.getCurrentUser().getEmail();
        return incidentRepository.countByCitoyenEmailAndStatut(email, StatutIncident.EN_RESOLUTION);
    }

    @Override
    public int countResoluForCurrentUser() {
        String email = currentUserService.getCurrentUser().getEmail();
        return incidentRepository.countByCitoyenEmailAndStatut(email, StatutIncident.RESOLU);
    }

    @Override
    public int countClotureForCurrentUser() {
        String email = currentUserService.getCurrentUser().getEmail();
        return incidentRepository.countByCitoyenEmailAndStatut(email, StatutIncident.CLOTURE);
    }
}
