package com.ville.gestionincidents.mapper;

import com.ville.gestionincidents.dto.incident.IncidentCreateDto;
import com.ville.gestionincidents.entity.Incident;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Convertit un IncidentCreateDto en entité Incident.
 */
@Component
public class IncidentMapper {

    public Incident toEntity(IncidentCreateDto dto) {
        Incident incident = new Incident();

        incident.setDescription(dto.getDescription());
        incident.setCategorie(dto.getCategorie());
        incident.setAdresse(dto.getAdresse());
        incident.setLatitude(dto.getLatitude());
        incident.setLongitude(dto.getLongitude());
        incident.setDateDeclaration(LocalDateTime.now());

        return incident;
    }
}
