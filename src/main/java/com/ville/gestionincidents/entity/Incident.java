package com.ville.gestionincidents.entity;

import com.ville.gestionincidents.enumeration.CategorieIncident;
import com.ville.gestionincidents.enumeration.PrioriteIncident;
import com.ville.gestionincidents.enumeration.StatutIncident;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@ToString(exclude = "notifications")
@EqualsAndHashCode(exclude = "notifications")
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;



    @Enumerated(EnumType.STRING)
    private StatutIncident statut;

    @Enumerated(EnumType.STRING)
    private PrioriteIncident priorite;


    private Double latitude;
    private Double longitude;

    private LocalDateTime dateDeclaration;
    private LocalDateTime dateDerniereMiseAJour;
    private LocalDateTime dateResolutionEstimee;
    private LocalDateTime dateResolution;


    // Relations
    @ManyToOne
    @JoinColumn(name = "citoyen_id")
    private Utilisateur citoyen;

    @ManyToOne
    private Utilisateur agent;

    // 🔹 CATÉGORIE = DÉPARTEMENT
    @ManyToOne(optional = false)
    @JoinColumn(name = "departement_id")
    private Departement departement;

    // 🔹 LOCALISATION
    @ManyToOne(optional = false)
    @JoinColumn(name = "quartier_id")
    private Quartier quartier;

    @ManyToOne
    private ServiceMunicipal service;

    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL)
    private List<Photo> photos;

    @OneToMany(mappedBy = "incident")
    private List<Notification> notifications;





    // getters/setters
}
