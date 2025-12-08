package com.ville.gestionincidents.entity;

import com.ville.gestionincidents.enumeration.Role;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String prenom;
    private String nom;

    @Column(unique = true)
    private String email;

    private String motDePasse;

    @Enumerated(EnumType.STRING)
    private Role role;

    private boolean emailVerifie = false;


    private String verificationToken;
    private LocalDateTime verificationTokenExpiration;
}
