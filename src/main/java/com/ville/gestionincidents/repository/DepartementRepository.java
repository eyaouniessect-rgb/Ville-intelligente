package com.ville.gestionincidents.repository;

import com.ville.gestionincidents.entity.Departement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartementRepository
        extends JpaRepository<Departement, Long> {
}
