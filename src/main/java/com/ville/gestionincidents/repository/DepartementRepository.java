package com.ville.gestionincidents.repository;

import com.ville.gestionincidents.entity.Departement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepartementRepository extends JpaRepository<Departement, Long> {

    /**
     * Recherche d'un département par nom
     */
    Optional<Departement> findByNom(String nom);

    /**
     * Vérifie si un département existe avec ce nom
     */
    boolean existsByNom(String nom);
}