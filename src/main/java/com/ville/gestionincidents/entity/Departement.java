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
@Table(name = "departements")
public class Departement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom du département est obligatoire")
    @Size(min = 3, max = 100, message = "Le nom doit contenir entre 3 et 100 caractères")
    @Column(nullable = false, unique = true)
    private String nom;

    @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères")
    @Column(length = 500)
    private String description;

    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String telephone;


    // Relation avec les services municipaux
    @OneToMany(mappedBy = "departement", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ServiceMunicipal> services = new ArrayList<>();


    // ==================== MÉTHODES UTILITAIRES ====================

    /**
     * Ajouter un service au département
     */
    public void addService(ServiceMunicipal service) {
        services.add(service);
        service.setDepartement(this);
    }

    /**
     * Retirer un service du département
     */
    public void removeService(ServiceMunicipal service) {
        services.remove(service);
        service.setDepartement(null);
    }

    @Override
    public String toString() {
        return "Departement{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", nbServices=" + (services != null ? services.size() : 0) +
                '}';
    }
}