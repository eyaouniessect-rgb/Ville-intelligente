package com.ville.gestionincidents.entity;

import com.ville.gestionincidents.enumeration.FormatRapport;
import lombok.*;

import javax.persistence.*;  // ← UTILISEZ UNIQUEMENT javax.persistence
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity  // ← javax.persistence.Entity
@Table(name = "rapport")  // ← javax.persistence.Table
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rapport {

    @Id  // ← javax.persistence.Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titre;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin", nullable = false)
    private LocalDate dateFin;

    @Column(name = "date_generation", nullable = false)
    private LocalDateTime dateGeneration;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FormatRapport format;

    @Column(name = "nom_fichier", nullable = false)
    private String nomFichier;

    @Column(name = "chemin_fichier", nullable = false)
    private String cheminFichier;

    @Column(name = "taille_fichier")
    private Long tailleFichier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departement_id")
    private Departement departement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private ServiceMunicipal service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quartier_id")
    private Quartier quartier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "genere_par")
    private Utilisateur generePar;

    @Column(name = "total_incidents")
    private Long totalIncidents;

    @Column(name = "incidents_resolus")
    private Long incidentsResolus;

    @Column(name = "incidents_en_cours")
    private Long incidentsEnCours;

    @Column(name = "delai_moyen")
    private Double delaiMoyen;
}