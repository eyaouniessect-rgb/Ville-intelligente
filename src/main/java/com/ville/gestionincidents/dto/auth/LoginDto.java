package com.ville.gestionincidents.dto.auth;

import lombok.Data;
import javax.validation.constraints.*;

@Data
public class LoginDto {

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    private String motDePasse;
}