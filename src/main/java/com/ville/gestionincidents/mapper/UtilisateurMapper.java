package com.ville.gestionincidents.mapper;

import com.ville.gestionincidents.dto.auth.RegisterDto;
import com.ville.gestionincidents.dto.utilisateur.citoyen.CitoyenProfilDto;
import com.ville.gestionincidents.dto.utilisateur.citoyen.CitoyenUpdateProfilDto;
import com.ville.gestionincidents.dto.utilisateur.superAdmin.CreateUtilisateurByAdminDto;
import com.ville.gestionincidents.dto.utilisateur.superAdmin.UpdateUtilisateurByAdminDto;
import com.ville.gestionincidents.dto.utilisateur.UtilisateurDto;
import com.ville.gestionincidents.entity.Utilisateur;
import com.ville.gestionincidents.enumeration.Role;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UtilisateurMapper {

    private final PasswordEncoder passwordEncoder;

    // Affichage (Entity → DTO)
    public UtilisateurDto toDTO(Utilisateur u) {
        UtilisateurDto dto = new UtilisateurDto();
      //  dto.setId(u.getId());
        dto.setNom(u.getNom());
        dto.setPrenom(u.getPrenom());
        dto.setEmail(u.getEmail());
      //  dto.setRole(u.getRole().name());
        return dto;
    }

    // Inscription citoyen (RegisterDto → Entity)
    public Utilisateur toEntity(RegisterDto dto) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNom(dto.getNom());
        utilisateur.setPrenom(dto.getPrenom());
        utilisateur.setEmail(dto.getEmail());
        utilisateur.setMotDePasse(passwordEncoder.encode(dto.getMotDePasse()));
        utilisateur.setRole(Role.CITOYEN);
        utilisateur.setEmailVerifie(false);
        return utilisateur;
    }

    // Entity → CitoyenProfilDto
    public CitoyenProfilDto toCitoyenProfilDto(Utilisateur user) {
        CitoyenProfilDto dto = new CitoyenProfilDto();
        dto.setNom(user.getNom());
        dto.setPrenom(user.getPrenom());
        dto.setEmail(user.getEmail());

        return dto;
    }

    // Update DTO → Entity
    public void updateCitoyenProfil(Utilisateur user,
                                    CitoyenUpdateProfilDto dto) {
        user.setNom(dto.getNom());
        user.setPrenom(dto.getPrenom());
        user.setEmail(dto.getEmail());
    }


    //  Création par SuperAdmin (CreateUtilisateurByAdminDto → Entity)
    public Utilisateur toEntityByAdmin(CreateUtilisateurByAdminDto dto, Role role) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNom(dto.getNom());
        utilisateur.setPrenom(dto.getPrenom());
        utilisateur.setEmail(dto.getEmail());
        utilisateur.setMotDePasse(passwordEncoder.encode(dto.getMotDePasse()));
        utilisateur.setRole(role);
        utilisateur.setEmailVerifie(true); // Pré-vérifié par admin

        // Champs optionnels
      /*  if (dto.getTelephone() != null) {
            utilisateur.setTelephone(dto.getTelephone());
        }
        if (dto.getAdresse() != null) {
            utilisateur.setAdresse(dto.getAdresse());
        }
        if (dto.getDepartement() != null && role == Role.AGENT) {
            utilisateur.setDepartement(dto.getDepartement());
        }*/

        return utilisateur;
    }
    public void updateEntityFromDto(Utilisateur utilisateur,
                                    UpdateUtilisateurByAdminDto dto) {

        utilisateur.setNom(dto.getNom());
        utilisateur.setPrenom(dto.getPrenom());
        utilisateur.setEmail(dto.getEmail());
        utilisateur.setRole(dto.getRole());

    }
}