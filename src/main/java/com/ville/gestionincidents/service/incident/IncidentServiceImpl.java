package com.ville.gestionincidents.service.incident;

import com.ville.gestionincidents.entity.Incident;
import com.ville.gestionincidents.enumeration.StatutIncident;
import com.ville.gestionincidents.repository.IncidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IncidentServiceImpl implements IncidentService {

    // Injection correcte du repository
    private final IncidentRepository incidentRepository;

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
