package com.ville.gestionincidents.repository;

import com.ville.gestionincidents.entity.Departement;
import com.ville.gestionincidents.entity.ServiceMunicipal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServiceMunicipalRepository extends JpaRepository<ServiceMunicipal, Long> {

    // 🔎 Trouver les services d’un département
    List<ServiceMunicipal> findByDepartement(Departement departement);

    // 🔎 Vérifier s’il existe déjà un service avec le même nom dans un département
    Optional<ServiceMunicipal> findByNomAndDepartement(String nom, Departement departement);

    // 📊 Compter les services d’un département
    long countByDepartement(Departement departement);

}
