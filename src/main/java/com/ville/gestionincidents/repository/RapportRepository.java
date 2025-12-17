package com.ville.gestionincidents.repository;

import com.ville.gestionincidents.entity.Departement;
import com.ville.gestionincidents.entity.Rapport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RapportRepository extends JpaRepository<Rapport, Long> {

    List<Rapport> findByDepartementOrderByDateGenerationDesc(Departement departement);

    List<Rapport> findByDepartementAndDateGenerationBetween(
            Departement departement,
            LocalDateTime debut,
            LocalDateTime fin
    );

    List<Rapport> findByGenerePar_IdOrderByDateGenerationDesc(Long userId);
}