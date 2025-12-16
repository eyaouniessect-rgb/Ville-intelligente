package com.ville.gestionincidents.dto.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * DTO pour la création et l'édition d'un service municipal
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceMunicipalDto {

    private Long id;

    @NotBlank(message = "Le nom du service est obligatoire")
    @Size(min = 3, max = 100, message = "Le nom doit contenir entre 3 et 100 caractères")
    private String nom;

    @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères")
    private String description;

    @NotNull(message = "Le département est obligatoire")
    private Long departementId;

    // Informations en lecture seule pour l'affichage
    private String departementNom;

    private Integer nombreAgents;

    private Integer nombreIncidents;
}