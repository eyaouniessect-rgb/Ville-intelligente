package com.ville.gestionincidents.entity;

import com.ville.gestionincidents.enumeration.CategorieIncident;
import com.ville.gestionincidents.enumeration.PrioriteIncident;
import com.ville.gestionincidents.enumeration.StatutIncident;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    @Enumerated(EnumType.STRING)
    private CategorieIncident categorie;

    @Enumerated(EnumType.STRING)
    private StatutIncident statut;

    @Enumerated(EnumType.STRING)
    private PrioriteIncident priorite;

    private String adresse;
    private Double latitude;
    private Double longitude;

    private LocalDateTime dateDeclaration;
    private LocalDateTime dateDerniereMiseAJour;
    private LocalDateTime dateResolutionEstimee;

    // Relations
    @ManyToOne
    private Utilisateur citoyen;

    @ManyToOne
    private Utilisateur agent;

    @ManyToOne
    private Quartier quartier;

    @ManyToOne
    private ServiceMunicipal service;

    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL)
    private List<Photo> photos;

    @OneToMany(mappedBy = "incident")
    private List<Notification> notifications;

    @OneToMany(mappedBy = "incident")
    private List<Rapport> rapports;

    // getters/setters
}
