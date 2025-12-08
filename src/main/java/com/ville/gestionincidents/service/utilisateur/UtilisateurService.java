package com.ville.gestionincidents.service.utilisateur;

import com.ville.gestionincidents.dto.auth.RegisterDto;

public interface UtilisateurService {
    boolean register(RegisterDto dto);
}
