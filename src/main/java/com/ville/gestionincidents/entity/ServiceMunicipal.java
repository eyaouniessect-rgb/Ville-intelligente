package com.ville.gestionincidents.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
public class ServiceMunicipal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom du service est obligatoire")
    @Size(min = 3, max = 100, message = "Le nom doit contenir entre 3 et 100 caractères")
    @Column(nullable = false)
    private String nom;

    @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères")
    @Column(length = 500)
    private String description;


    /**
     * Département auquel appartient ce service
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departement_id", nullable = false)
    private Departement departement;

    /**
     * Agents assignés à ce service
     */
     @OneToMany(mappedBy = "serviceMunicipal")
     private List<Utilisateur> agents = new ArrayList<>();

    /**
     * Incidents gérés par ce service
     */
    @OneToMany(mappedBy = "service")
    private List<Incident> incidents = new ArrayList<>();



    // ==================== MÉTHODES UTILITAIRES ====================


    /**
     * Obtenir le nombre d'agents assignés
     */
    public int getNombreAgents() {
        return agents != null ? agents.size() : 0;
    }

    /**
     * Obtenir le nombre d'incidents
     */
    public int getNombreIncidents() {
        return incidents != null ? incidents.size() : 0;
    }



    @Override
    public String toString() {
        return "ServiceMunicipal{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", departement=" + (departement != null ? departement.getNom() : "null") +
                ", nbAgents=" + getNombreAgents() +
                ", nbIncidents=" + getNombreIncidents() +
                '}';
    }



}
