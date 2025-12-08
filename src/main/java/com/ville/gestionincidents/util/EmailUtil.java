package com.ville.gestionincidents.util;

import org.springframework.stereotype.Component;

@Component
public class EmailUtil {

    public void sendVerificationEmail(String to, String token) {
        System.out.println("==== LIEN DE VÉRIFICATION ====");
        System.out.println("Clique ici : http://localhost:8080/api/auth/verify?token=" + token);
    }
}
