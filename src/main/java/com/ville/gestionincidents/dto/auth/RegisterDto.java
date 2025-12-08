package com.ville.gestionincidents.dto.auth;
import lombok.Data;

@Data
public class RegisterDto {

    private String Nom;
    private String Prenom;
    private String email;
    private String motDePasse;

}
