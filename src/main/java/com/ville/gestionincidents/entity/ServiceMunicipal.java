package com.ville.gestionincidents.entity;

import javax.persistence.*;
import java.util.List;

@Entity
public class ServiceMunicipal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String description;

    @OneToMany(mappedBy = "service")
    private List<Incident> incidents;

    @ManyToOne
    private Departement departement;

    // getters/setters
}
