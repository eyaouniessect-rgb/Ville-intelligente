package com.ville.gestionincidents.repository;

import com.ville.gestionincidents.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    Optional<Utilisateur> findByEmail(String email);

    // ✅ AJOUT pour la vérification d'email
    Optional<Utilisateur> findByVerificationToken(String token);
}