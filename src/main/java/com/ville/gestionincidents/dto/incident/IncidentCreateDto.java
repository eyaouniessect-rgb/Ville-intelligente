package com.ville.gestionincidents.dto.incident;

import com.ville.gestionincidents.enumeration.CategorieIncident;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

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

    private MultipartFile photo;



}