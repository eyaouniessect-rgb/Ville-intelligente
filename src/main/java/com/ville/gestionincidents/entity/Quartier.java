package com.ville.gestionincidents.entity;

import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Pattern;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "quartier")
public class Quartier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;




    @NotBlank(message = "Le nom du quartier est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    @Column(nullable = false, unique = true)
    private String nom;

    @NotBlank(message = "Le code postal est obligatoire")
    @Pattern(regexp = "^[0-9]{4}$", message = "Le code postal doit contenir exactement 4 chiffres")
    @Column(nullable = false, length = 4)
    private String codePostal;

    @OneToMany(mappedBy = "quartier", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Incident> incidents = new ArrayList<>();
    @ManyToOne
    @JoinColumn(name = "departement_id")
    private Departement departement;

    // ==================== MÉTHODES UTILITAIRES ====================

    public int getNombreIncidents() {
        return incidents != null ? incidents.size() : 0;
    }

    @Override
    public String toString() {
        return "Quartier{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", codePostal='" + codePostal + '\'' +
                ", nbIncidents=" + getNombreIncidents() +
                '}';
    }
}