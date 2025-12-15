package com.ville.gestionincidents.repository;

import com.ville.gestionincidents.entity.Quartier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuartierRepository extends JpaRepository<Quartier, Long> {

    /**
     * Vérifier si un quartier avec ce nom existe déjà
     */
    boolean existsByNom(String nom);

    /**
     * Trouver un quartier par son nom
     */
    Optional<Quartier> findByNom(String nom);

    /**
     * Trouver les quartiers par code postal
     */
    java.util.List<Quartier> findByCodePostal(String codePostal);

    /**
     * Compter les quartiers
     */
    long count();
}