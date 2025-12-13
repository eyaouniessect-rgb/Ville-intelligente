package com.ville.gestionincidents.security;

import com.ville.gestionincidents.entity.Utilisateur;

public interface CurrentUserService {
    Utilisateur getCurrentUser();
    Long getCurrentUserId();
}
