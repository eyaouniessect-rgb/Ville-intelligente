package com.ville.gestionincidents.entity;

import javax.persistence.*;
import java.util.List;

@Entity
public class Quartier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String codePostal;

    @OneToMany(mappedBy = "quartier")
    private List<Incident> incidents;

    // getters/setters
}
