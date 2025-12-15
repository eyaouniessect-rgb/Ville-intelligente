package com.ville.gestionincidents.entity;

import com.ville.gestionincidents.enumeration.AuthProvider;
import com.ville.gestionincidents.enumeration.Role;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity //  Indique que cette classe est une entité JPA (table en base)
@Data  // Lombok : génère getters, setters, toString, equals, hashCode
@ToString(exclude = "notifications")
@EqualsAndHashCode(exclude = "notifications")
public class Utilisateur {

    @Id //  Clé primaire
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // ID auto-incrémenté par la base de données
    private Long id;

    private String prenom;
    private String nom;

    @Column(unique = true)
    private String email;

    private String motDePasse;

    @Enumerated(EnumType.STRING)
    //  Stocke le rôle sous forme de texte (ADMIN, AGENT, etc.)
    private Role role;

    private boolean emailVerifie = false;
    // Sert aussi à activer / désactiver un compte

    /**
     * Lien avec le département
     *
     * - Un ADMIN appartient à UN seul département
     * - Un département peut avoir PLUSIEURS admins
     */
    @ManyToOne
    @JoinColumn(name = "departement_id")
    //  Colonne FK dans la table utilisateur
    private Departement departement;

    @ManyToOne
    @JoinColumn(name = "service_municipal_id")
    private ServiceMunicipal serviceMunicipal;

    //  Champs utilisés pour la vérification d'email
    private String verificationToken;
    private LocalDateTime verificationTokenExpiration;


    // ✅ À AJOUTER après la ligne : private LocalDateTime verificationTokenExpiration;

    // NOUVEAUX CHAMPS POUR OAUTH2
    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider")
    private AuthProvider authProvider = AuthProvider.LOCAL;

    @Column(name = "provider_id")
    private String providerId;

    @Column(name = "image_url")
    private String imageUrl;

    @Transient
    public String getFullName() {
        return (prenom != null ? prenom : "") + " " + (nom != null ? nom : "");
    }

    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL)
    private List<Notification> notifications;

    // 🔔 Préférences de notification
    @OneToOne(mappedBy = "utilisateur", cascade = CascadeType.ALL)
    private PreferenceNotification preferenceNotification;


}
