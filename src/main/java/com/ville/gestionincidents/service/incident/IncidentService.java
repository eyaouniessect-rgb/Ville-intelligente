package com.ville.gestionincidents.service.incident;

import com.ville.gestionincidents.entity.Incident;

import java.util.List;

public interface IncidentService {

    int countByEmail(String email); // Total incidents

    int countInProgress(String email); // Nombre incidents en cours

    int countResolved(String email); // Nombre incidents résolus

    List<Incident> findByCitoyenEmail(String email); // Liste incidents

    Incident findByIdAndCheckOwner(Long id, String email); // Vérification propriétaire
}
