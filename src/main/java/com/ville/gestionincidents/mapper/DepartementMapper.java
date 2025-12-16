package com.ville.gestionincidents.mapper;

import com.ville.gestionincidents.dto.departement.DepartementDto;
import com.ville.gestionincidents.entity.Departement;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper pour convertir entre Departement et DepartementDto
 */
@Component
public class DepartementMapper {

    /**
     * Convertir une entité Departement en DTO
     */
    public DepartementDto toDto(Departement departement) {
        if (departement == null) {
            return null;
        }

        return DepartementDto.builder()
                .id(departement.getId())
                .nom(departement.getNom())
                .description(departement.getDescription())
                .email(departement.getEmail())
                .telephone(departement.getTelephone())
                .nombreServices(departement.getServices() != null ? departement.getServices().size() : 0)
                .build();
    }

    /**
     * Convertir un DTO en entité Departement
     */
    public Departement toEntity(DepartementDto dto) {
        if (dto == null) {
            return null;
        }

        Departement departement = new Departement();
        departement.setId(dto.getId());
        departement.setNom(dto.getNom());
        departement.setDescription(dto.getDescription());
        departement.setEmail(dto.getEmail());
        departement.setTelephone(dto.getTelephone());

        return departement;
    }

    /**
     * Mettre à jour une entité existante avec les données du DTO
     */
    public void updateEntityFromDto(DepartementDto dto, Departement departement) {
        if (dto == null || departement == null) {
            return;
        }

        departement.setNom(dto.getNom());
        departement.setDescription(dto.getDescription());
        departement.setEmail(dto.getEmail());
        departement.setTelephone(dto.getTelephone());
    }

    /**
     * Convertir une liste d'entités en liste de DTOs
     */
    public List<DepartementDto> toDtoList(List<Departement> departements) {
        if (departements == null) {
            return null;
        }

        return departements.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}