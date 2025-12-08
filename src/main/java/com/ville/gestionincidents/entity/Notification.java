package com.ville.gestionincidents.entity;

import com.ville.gestionincidents.enumeration.TypeNotification;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TypeNotification type;

    private String message;
    private LocalDateTime dateEnvoi;
    private boolean lu;

    @ManyToOne
    private Utilisateur utilisateur;

    @ManyToOne
    private Incident incident;

    // getters/setters
}
