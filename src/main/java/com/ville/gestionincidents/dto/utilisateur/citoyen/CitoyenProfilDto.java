package com.ville.gestionincidents.dto.utilisateur.citoyen;
import lombok.Data;

@Data
public class CitoyenProfilDto {

    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String adresse;
}
