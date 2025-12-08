package com.ville.gestionincidents.service.utilisateur;

import com.ville.gestionincidents.dto.auth.RegisterDto;
import com.ville.gestionincidents.entity.Utilisateur;
import com.ville.gestionincidents.enumeration.Role;
import com.ville.gestionincidents.repository.UtilisateurRepository;
import com.ville.gestionincidents.service.utilisateur.UtilisateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UtilisateurServiceImpl implements UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public boolean register(RegisterDto dto) {
        if (utilisateurRepository.existsByEmail(dto.getEmail())) {
            return false; // email déjà utilisé
        }

        Utilisateur user = new Utilisateur();
        user.setNom(dto.getNom());
        user.setPrenom(dto.getPrenom());
        user.setEmail(dto.getEmail());
        user.setMotDePasse(passwordEncoder.encode(dto.getMotDePasse()));
        user.setRole(Role.CITOYEN);
        //user.setActif(true);

        utilisateurRepository.save(user);
        return true;
    }
}
