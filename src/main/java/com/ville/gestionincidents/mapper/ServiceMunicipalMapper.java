package com.ville.gestionincidents.mapper;

import com.ville.gestionincidents.dto.service.ServiceMunicipalDto;
import com.ville.gestionincidents.entity.Departement;
import com.ville.gestionincidents.entity.ServiceMunicipal;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper pour convertir entre ServiceMunicipal et ServiceMunicipalDto
 */
@Component
public class ServiceMunicipalMapper {

    /**
     * Convertir une entité ServiceMunicipal en DTO
     */
    public ServiceMunicipalDto toDto(ServiceMunicipal service) {
        if (service == null) {
            return null;
        }

        return ServiceMunicipalDto.builder()
                .id(service.getId())
                .nom(service.getNom())
                .description(service.getDescription())
                .departementId(service.getDepartement() != null ? service.getDepartement().getId() : null)
                .departementNom(service.getDepartement() != null ? service.getDepartement().getNom() : null)
                .nombreAgents(service.getNombreAgents())
                .nombreIncidents(service.getNombreIncidents())
                .build();
    }

    /**
     * Convertir un DTO en entité ServiceMunicipal
     * Note: Le département doit être défini séparément
     */
    public ServiceMunicipal toEntity(ServiceMunicipalDto dto) {
        if (dto == null) {
            return null;
        }

        ServiceMunicipal service = new ServiceMunicipal();
        service.setId(dto.getId());
        service.setNom(dto.getNom());
        service.setDescription(dto.getDescription());

        // Le département sera défini par le service métier
        return service;
    }

    /**
     * Convertir un DTO en entité avec le département
     */
    public ServiceMunicipal toEntity(ServiceMunicipalDto dto, Departement departement) {
        if (dto == null) {
            return null;
        }

        ServiceMunicipal service = toEntity(dto);
        service.setDepartement(departement);
        return service;
    }

    /**
     * Mettre à jour une entité existante avec les données du DTO
     */
    public void updateEntityFromDto(ServiceMunicipalDto dto, ServiceMunicipal service) {
        if (dto == null || service == null) {
            return;
        }

        service.setNom(dto.getNom());
        service.setDescription(dto.getDescription());
        // Le département n'est pas modifié lors d'une mise à jour
    }

    /**
     * Convertir une liste d'entités en liste de DTOs
     */
    public List<ServiceMunicipalDto> toDtoList(List<ServiceMunicipal> services) {
        if (services == null) {
            return null;
        }

        return services.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}