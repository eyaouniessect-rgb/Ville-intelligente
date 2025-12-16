package com.ville.gestionincidents.dto.departement;

import com.ville.gestionincidents.entity.ServiceMunicipal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.CascadeType;
import javax.persistence.OneToMany;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO pour la création et l'édition d'un département
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartementDto {

    private Long id;

    @NotBlank(message = "Le nom du département est obligatoire")
    @Size(min = 3, max = 100, message = "Le nom doit contenir entre 3 et 100 caractères")
    private String nom;

    @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères")
    private String description;

    @Email(message = "L'email doit être valide")
    @Size(max = 100, message = "L'email ne peut pas dépasser 100 caractères")
    private String email;

    @Size(max = 20, message = "Le téléphone ne peut pas dépasser 20 caractères")
    private String telephone;

    // Nombre de services (pour l'affichage uniquement)
    private Integer nombreServices;
}