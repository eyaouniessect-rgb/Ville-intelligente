package com.ville.gestionincidents.dto.utilisateur.citoyen;


import lombok.Data;
import javax.validation.constraints.*;

@Data
public class CitoyenUpdateProfilDto {

    @NotBlank
    @Size(min = 2, max = 50)
    private String prenom;

    @NotBlank
    @Size(min = 2, max = 50)
    private String nom;

    @NotBlank
    @Email
    private String email;

    private String telephone;
    private String adresse;
}
