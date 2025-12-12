package com.ville.gestionincidents.repository;

import com.ville.gestionincidents.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import com.ville.gestionincidents.enumeration.Role;
import java.util.List;
import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    Optional<Utilisateur> findByEmail(String email);

    // ✅ AJOUT pour la vérification d'email
    Optional<Utilisateur> findByVerificationToken(String token);

    /**
     * ✅ AJOUTER : Recherche des utilisateurs par rôle
     */
    List<Utilisateur> findByRole(Role role);

    /**
     * ✅ AJOUTER : Compte le nombre d'utilisateurs par rôle
     */
    long countByRole(Role role);

    /**
     * Vérifie si un email existe déjà
     */
    boolean existsByEmail(String email);
}