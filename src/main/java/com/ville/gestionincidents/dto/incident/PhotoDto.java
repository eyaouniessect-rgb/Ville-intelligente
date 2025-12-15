package com.ville.gestionincidents.dto.incident;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO léger pour exposer les photos d'un incident sans renvoyer l'entité.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoDto {

    private Long id;
    private String nomFichier;
    private String cheminStockage;
    private boolean principale;
}

