package com.ville.gestionincidents.mapper;

import com.ville.gestionincidents.dto.utilisateur.UtilisateurDto;
import com.ville.gestionincidents.entity.Utilisateur;
import org.springframework.stereotype.Component;

@Component
public class UtilisateurMapper {

    public UtilisateurDto toDTO(Utilisateur u) {

        UtilisateurDto dto = new UtilisateurDto();



        //dto.setId(u.getId());
        dto.setNom(u.getNom());
        dto.setPrenom(u.getPrenom());
        dto.setEmail(u.getEmail());
      //  dto.setRole(u.getRole().name());




        return dto;
    }
}
