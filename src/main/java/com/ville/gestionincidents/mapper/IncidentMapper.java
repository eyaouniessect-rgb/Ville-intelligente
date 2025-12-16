package com.ville.gestionincidents.mapper;

import com.ville.gestionincidents.dto.incident.IncidentCreateDto;
import com.ville.gestionincidents.dto.incident.IncidentDetailsDto;
import com.ville.gestionincidents.dto.incident.IncidentListDto;
import com.ville.gestionincidents.dto.incident.PhotoDto;
import com.ville.gestionincidents.entity.Departement;
import com.ville.gestionincidents.entity.Incident;
import com.ville.gestionincidents.entity.Photo;
import com.ville.gestionincidents.entity.Quartier;
import com.ville.gestionincidents.repository.DepartementRepository;
import com.ville.gestionincidents.repository.QuartierRepository;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class IncidentMapper {

    private final DepartementRepository departementRepository;
    private final QuartierRepository quartierRepository;

    /* ===================== CREATE ===================== */

    public Incident toEntity(IncidentCreateDto dto) {

        Incident incident = new Incident();

        incident.setDescription(dto.getDescription());
        incident.setLatitude(dto.getLatitude());
        incident.setLongitude(dto.getLongitude());
        incident.setDateDeclaration(LocalDateTime.now());

        // 🔗 Département (ancienne "catégorie")
        Departement departement = departementRepository.findById(dto.getDepartementId())
                .orElseThrow(() -> new IllegalArgumentException("Département introuvable"));
        incident.setDepartement(departement);

        // 🔗 Quartier
        Quartier quartier = quartierRepository.findById(dto.getQuartierId())
                .orElseThrow(() -> new IllegalArgumentException("Quartier introuvable"));
        incident.setQuartier(quartier);

        return incident;
    }

    /* ===================== LIST ===================== */

    public IncidentListDto toListDto(Incident incident) {
        return IncidentListDto.builder()
                .id(incident.getId())
                .description(incident.getDescription())
                .statut(incident.getStatut())
                .dateDeclaration(incident.getDateDeclaration())
                .departementNom(
                        incident.getDepartement() != null
                                ? incident.getDepartement().getNom()
                                : null
                )
                .quartierNom(
                        incident.getQuartier() != null
                                ? incident.getQuartier().getNom()
                                : null
                )
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

    /* ===================== DETAILS ===================== */

    public IncidentDetailsDto toDetailsDto(Incident incident) {
        return IncidentDetailsDto.builder()
                .id(incident.getId())
                .description(incident.getDescription())
                .statut(incident.getStatut())
                .latitude(incident.getLatitude())
                .longitude(incident.getLongitude())
                .dateDeclaration(incident.getDateDeclaration())
                .dateDerniereMiseAJour(incident.getDateDerniereMiseAJour())
                .dateResolutionEstimee(incident.getDateResolutionEstimee())
                .departementNom(
                        incident.getDepartement() != null
                                ? incident.getDepartement().getNom()
                                : null
                )
                .quartierNom(
                        incident.getQuartier() != null
                                ? incident.getQuartier().getNom()
                                : null
                )
                .photos(mapPhotos(incident.getPhotos()))
                .build();
    }

    /* ===================== HELPERS ===================== */

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
