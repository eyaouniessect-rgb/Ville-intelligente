package com.ville.gestionincidents.dto.utilisateur.citoyen;

import lombok.Data;
import javax.validation.constraints.*;

@Data
public class ChangePasswordDto {

    @NotBlank
    private String ancienMotDePasse;

    @NotBlank
    @Size(min = 12, message = "Minimum 12 caractères")
    private String nouveauMotDePasse;

    @NotBlank
    private String confirmationMotDePasse;
}
