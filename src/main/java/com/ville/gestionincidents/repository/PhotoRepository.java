package com.ville.gestionincidents.repository;

import com.ville.gestionincidents.entity.Photo;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * CRUD pour photos associées aux incidents.
 */
public interface PhotoRepository extends JpaRepository<Photo, Long> {
}
