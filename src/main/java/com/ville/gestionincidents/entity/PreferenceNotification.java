package com.ville.gestionincidents.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class PreferenceNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean emailActif= true;
    private boolean emailChangementStatut= true;
    private boolean pushActif =true;

    @OneToOne
    @JoinColumn(
            name = "utilisateur_id",
            nullable = false,
            unique = true
    )
    private Utilisateur utilisateur;

    // getters/setters
}
