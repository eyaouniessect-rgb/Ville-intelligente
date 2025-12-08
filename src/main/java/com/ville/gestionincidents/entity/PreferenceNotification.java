package com.ville.gestionincidents.entity;

import javax.persistence.*;

@Entity
public class PreferenceNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean emailActif;
    private boolean emailChangementStatut;
    private boolean emailNouveauCommentaire;
    private boolean pushActif;

    @OneToOne
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    // getters/setters
}
