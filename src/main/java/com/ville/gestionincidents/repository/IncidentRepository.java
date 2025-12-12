package com.ville.gestionincidents.repository;
import com.ville.gestionincidents.entity.Incident;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Interface permettant d'effectuer des opérations CRUD sur Incident.
 */
public interface IncidentRepository extends JpaRepository<Incident, Long> {
}