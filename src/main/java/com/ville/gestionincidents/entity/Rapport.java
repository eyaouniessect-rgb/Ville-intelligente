package com.ville.gestionincidents.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Rapport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;
    private String type; // ou enum si tu veux
    private LocalDateTime dateGeneration;
    @Lob
    private String contenu;

    @ManyToOne
    private Incident incident;

    // getters/setters
}
