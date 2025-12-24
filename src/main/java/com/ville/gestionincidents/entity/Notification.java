package com.ville.gestionincidents.entity;

import com.ville.gestionincidents.enumeration.TypeNotification;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;
@Data
@Entity
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TypeNotification type;

    private String message;
    private LocalDateTime dateEnvoi;
    private boolean lu;

    @ManyToOne
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    @ManyToOne
    @JoinColumn(name = "incident_id")
    private Incident incident;


}
