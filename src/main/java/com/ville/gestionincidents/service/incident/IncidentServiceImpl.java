package com.ville.gestionincidents.service.incident;

import com.ville.gestionincidents.dto.incident.IncidentCreateDto;
import com.ville.gestionincidents.entity.Incident;
import com.ville.gestionincidents.entity.Photo;
import com.ville.gestionincidents.enumeration.StatutIncident;
import com.ville.gestionincidents.mapper.IncidentMapper;
import com.ville.gestionincidents.repository.IncidentRepository;
import com.ville.gestionincidents.repository.PhotoRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    @Override
    public void creerIncident(IncidentCreateDto dto) {

        // 1️⃣ Convertir le DTO en entité Incident
        Incident incident = incidentMapper.toEntity(dto);

        // 2️⃣ Sauvegarder l'incident dans la BD
        incidentRepository.save(incident);

        // 3️⃣ Si une photo a été uploadée → la sauvegarder
        if (dto.getPhoto() != null && !dto.getPhoto().isEmpty()) {

            // --- 3.1 Sauvegarde physique dans /uploads/ ---
            String nomFichier = photoStorageService.save(dto.getPhoto());

            // Chemin complet : uploads/nomFichier
            String cheminStockage = "uploads/" + nomFichier;

            // --- 3.2 Enregistrement en BD ---
            Photo photo = new Photo();
            photo.setNomFichier(nomFichier);
            photo.setTypeContenu(dto.getPhoto().getContentType());
            photo.setCheminStockage(cheminStockage);
            photo.setPrincipale(true);    // la 1ère photo = photo principale
            photo.setIncident(incident);  // relation ManyToOne

            photoRepository.save(photo);
        }


    }

    @Override
    public int countByEmail(String email) {
        return incidentRepository.countByCitoyenEmail(email);
    }

    @Override
    public int countInProgress(String email) {
        return incidentRepository.countByCitoyenEmailAndStatut(email, StatutIncident.EN_RESOLUTION);
    }

    @Override
    public int countResolved(String email) {
        return incidentRepository.countByCitoyenEmailAndStatut(email, StatutIncident.RESOLU);
    }

    @Override
    public List<Incident> findByCitoyenEmail(String email) {
        return incidentRepository.findByCitoyenEmail(email);
    }

    @Override
    public Incident findByIdAndCheckOwner(Long id, String email) {

        // Récupérer incident ou erreur si non trouvé
        Incident inc = incidentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Incident introuvable"));

        // Vérification propriétaire
        if (!inc.getCitoyen().getEmail().equals(email)) {
            throw new RuntimeException("Accès non autorisé à cet incident !");
        }

        return inc;
    }


}
