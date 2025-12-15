package com.ville.gestionincidents.mapper;

import com.ville.gestionincidents.dto.incident.IncidentCreateDto;
import com.ville.gestionincidents.dto.incident.IncidentDetailsDto;
import com.ville.gestionincidents.dto.incident.IncidentListDto;
import com.ville.gestionincidents.dto.incident.PhotoDto;
import com.ville.gestionincidents.entity.Incident;
import com.ville.gestionincidents.entity.Photo;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Convertit les DTOs incident ↔ entités.
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

    public IncidentListDto toListDto(Incident incident) {
        return IncidentListDto.builder()
                .id(incident.getId())
                .description(incident.getDescription())
                .categorie(incident.getCategorie())
                .statut(incident.getStatut())
                .adresse(incident.getAdresse())
                .dateDeclaration(incident.getDateDeclaration())
                .photoPrincipale(resolvePhotoPrincipale(incident))
                .nombrePhotos(countPhotos(incident))
                .build();
    }

    public List<IncidentListDto> toListDtos(List<Incident> incidents) {
        if (incidents == null) {
            return Collections.emptyList();
        }
        return incidents.stream()
                .map(this::toListDto)
                .collect(Collectors.toList());
    }

    public IncidentDetailsDto toDetailsDto(Incident incident) {
        return IncidentDetailsDto.builder()
                .id(incident.getId())
                .description(incident.getDescription())
                .categorie(incident.getCategorie())
                .statut(incident.getStatut())
                .adresse(incident.getAdresse())
                .latitude(incident.getLatitude())
                .longitude(incident.getLongitude())
                .dateDeclaration(incident.getDateDeclaration())
                .dateDerniereMiseAJour(incident.getDateDerniereMiseAJour())
                .dateResolutionEstimee(incident.getDateResolutionEstimee())
                .photos(mapPhotos(incident.getPhotos()))
                .build();
    }

    private String resolvePhotoPrincipale(Incident incident) {
        if (incident.getPhotos() == null || incident.getPhotos().isEmpty()) {
            return null;
        }

        Optional<Photo> principale = incident.getPhotos()
                .stream()
                .filter(Photo::isPrincipale)
                .findFirst();

        return principale
                .map(Photo::getCheminStockage)
                .orElse(incident.getPhotos().get(0).getCheminStockage());
    }

    private int countPhotos(Incident incident) {
        return incident.getPhotos() == null ? 0 : incident.getPhotos().size();
    }

    private List<PhotoDto> mapPhotos(List<Photo> photos) {
        if (photos == null) {
            return Collections.emptyList();
        }

        return photos.stream()
                .map(photo -> PhotoDto.builder()
                        .id(photo.getId())
                        .nomFichier(photo.getNomFichier())
                        .cheminStockage(photo.getCheminStockage())
                        .principale(photo.isPrincipale())
                        .build())
                .collect(Collectors.toList());
    }
}
