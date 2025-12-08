package com.ville.gestionincidents.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class LogActivite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String action;
    private String details;
    private LocalDateTime dateAction;

    @ManyToOne
    private Utilisateur utilisateur;

    @ManyToOne
    private Incident incident;

    // getters/setters
}
