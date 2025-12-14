package com.ville.gestionincidents.dto.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceMunicipalDto {
    private Long id;
    private String nom;
    private String description;
    private Long departementId;
    private String departementNom;
}