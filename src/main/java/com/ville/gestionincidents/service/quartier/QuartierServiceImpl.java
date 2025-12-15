package com.ville.gestionincidents.service.quartier;

import com.ville.gestionincidents.entity.Quartier;
import com.ville.gestionincidents.entity.Incident;
import com.ville.gestionincidents.repository.QuartierRepository;
import com.ville.gestionincidents.repository.IncidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuartierServiceImpl implements QuartierService {

    private final QuartierRepository quartierRepository;
    private final IncidentRepository incidentRepository;

    // ==================== GESTION DES QUARTIERS ====================

    @Override
    @Transactional
    public Quartier createQuartier(Quartier quartier) {
        // Vérifier si le quartier existe déjà
        if (quartierRepository.existsByNom(quartier.getNom())) {
            throw new RuntimeException("Un quartier avec ce nom existe déjà");
        }

        Quartier saved = quartierRepository.save(quartier);
        return saved;
    }

    @Override
    @Transactional
    public Quartier updateQuartier(Long id, Quartier quartier) {
        Quartier existing = findById(id);

        // Vérifier si le nouveau nom existe déjà (sauf si c'est le même quartier)
        if (!existing.getNom().equals(quartier.getNom()) &&
                quartierRepository.existsByNom(quartier.getNom())) {
            throw new RuntimeException("Un quartier avec ce nom existe déjà");
        }

        existing.setNom(quartier.getNom());
        existing.setCodePostal(quartier.getCodePostal());

        Quartier updated = quartierRepository.save(existing);
        return updated;
    }

    @Override
    @Transactional
    public void deleteQuartier(Long id) {
        Quartier quartier = findById(id);

        // Vérifier s'il y a des incidents associés
        long incidentCount = incidentRepository.countByQuartier(quartier);
        if (incidentCount > 0) {
            throw new RuntimeException("Impossible de supprimer ce quartier. Il contient " + incidentCount + " incident(s).");
        }

        quartierRepository.deleteById(id);
    }

    @Override
    public Quartier findById(Long id) {
        return quartierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quartier introuvable avec l'ID : " + id));
    }

    @Override
    public List<Quartier> findAll() {
        return quartierRepository.findAll();
    }

    @Override
    public List<Quartier> findByCodePostal(String codePostal) {
        return quartierRepository.findByCodePostal(codePostal);
    }

    // ==================== STATISTIQUES ====================

    @Override
    public long countQuartiers() {
        return quartierRepository.count();
    }
}