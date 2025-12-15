package com.ville.gestionincidents.dto.dashboardAdmin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DelaiResolutionDto {
    private String nomService;
    private Double delaiMoyen; // en jours
}