package com.ville.gestionincidents.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nomFichier;
    private String typeContenu;
    private String cheminStockage;
    private LocalDateTime dateUpload;
    private boolean principale;

    @ManyToOne
    private Incident incident;

    // getters/setters
}
