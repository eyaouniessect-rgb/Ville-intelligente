package com.ville.gestionincidents.dto.dashboardAdmin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncidentParServiceDto {
    private String nomService;
    private Long nombre;
}