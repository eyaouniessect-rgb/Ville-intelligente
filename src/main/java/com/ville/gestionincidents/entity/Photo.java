package com.ville.gestionincidents.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String nomFichier;


    private String typeContenu;

    // Chemin dans dossier local : "uploads/nomimage.jpg"
    private String cheminStockage;

    private LocalDateTime dateUpload;

    // VRAI si c'est la photo principale de l'incident
    private boolean principale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_id")
    private Incident incident;

    @PrePersist
    public void prePersist() {
        this.dateUpload = LocalDateTime.now();
    }
}
