package com.ville.gestionincidents.entity;

import javax.persistence.*;
import java.util.List;

@Entity
public class Departement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String description;
    private String email;
    private String telephone;

    @OneToMany(mappedBy = "departement")
    private List<ServiceMunicipal> services;

    // getters/setters
}
