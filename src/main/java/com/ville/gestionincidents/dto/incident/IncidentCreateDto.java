package com.ville.gestionincidents.dto.incident;

import com.ville.gestionincidents.enumeration.CategorieIncident;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.*;
import java.util.List;

/**
 * DTO utilisé pour recevoir les données du formulaire citoyen.
 */
@Data
public class IncidentCreateDto {

    @NotBlank(message = "La description est obligatoire")
    @Size(min = 10, max = 1000, message = "La description doit contenir entre 10 et 1000 caractères")
    private String description;

    @NotNull(message = "La catégorie est obligatoire")
    private CategorieIncident categorie;

    @NotBlank(message = "L'adresse est obligatoire")
    @Size(min = 5, max = 255, message = "L'adresse doit contenir entre 5 et 255 caractères")
    private String adresse;

    @NotNull(message = "La latitude est obligatoire")
    @DecimalMin(value = "-90.0", message = "Latitude invalide")
    @DecimalMax(value = "90.0", message = "Latitude invalide")
    private Double latitude;

    @NotNull(message = "La longitude est obligatoire")
    @DecimalMin(value = "-180.0", message = "Longitude invalide")
    @DecimalMax(value = "180.0", message = "Longitude invalide")
    private Double longitude;

    private List<MultipartFile> photos;

}
