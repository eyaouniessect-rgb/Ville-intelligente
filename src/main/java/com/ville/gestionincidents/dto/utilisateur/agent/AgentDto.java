package com.ville.gestionincidents.dto.utilisateur.agent;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AgentDto {
    private Long id;
    private String prenom;
    private String nom;
}
