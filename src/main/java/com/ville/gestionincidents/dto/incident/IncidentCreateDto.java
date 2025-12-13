package com.ville.gestionincidents.dto.incident;

import com.ville.gestionincidents.enumeration.CategorieIncident;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * DTO utilisé pour recevoir les données du formulaire citoyen.
 */
@Data
public class IncidentCreateDto {

    private String description;
    private CategorieIncident categorie;
    private String adresse;

    private Double latitude;
    private Double longitude;

    // Permet d’uploader plusieurs images
    private List<MultipartFile> photos;



}