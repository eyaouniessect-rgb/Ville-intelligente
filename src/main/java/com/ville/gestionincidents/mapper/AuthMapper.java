package com.ville.gestionincidents.mapper;

import com.ville.gestionincidents.dto.auth.RegisterDto;
import com.ville.gestionincidents.entity.Utilisateur;
import com.ville.gestionincidents.enumeration.Role;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {

    public Utilisateur registerDtoToEntity(RegisterDto dto) {
        Utilisateur u = new Utilisateur();
        u.setNom(dto.getNom());
        u.setPrenom(dto.getPrenom());
        u.setEmail(dto.getEmail());
        u.setMotDePasse(dto.getMotDePasse()); // sera encodé dans le service
        u.setEmailVerifie(false);
        u.setRole(Role.CITOYEN); // rôle par défaut
        return u;
    }
}
