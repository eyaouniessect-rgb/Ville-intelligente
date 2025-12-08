package com.ville.gestionincidents.entity;

import com.ville.gestionincidents.enumeration.Role;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;

    private boolean actif = true;
    private boolean emailVerifie = false;

    @Enumerated(EnumType.STRING)
    private Role role;

    private LocalDateTime dateInscription;
    private LocalDateTime dateDerniereConnexion;

    // Relations
    @OneToMany(mappedBy = "citoyen")
    private List<Incident> incidentsDeclares;

    @OneToMany(mappedBy = "agent")
    private List<Incident> incidentsTraites;

    @OneToMany(mappedBy = "utilisateur")
    private List<Notification> notifications;

    @OneToMany(mappedBy = "utilisateur")
    private List<LogActivite> logsActivite;

    @OneToOne(mappedBy = "utilisateur", cascade = CascadeType.ALL)
    private PreferenceNotification preferencesNotification;

    // getters/setters vides pour l'instant (tu pourras générer avec IntelliJ)
}
