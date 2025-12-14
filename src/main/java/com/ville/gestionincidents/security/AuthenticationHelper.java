package com.ville.gestionincidents.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

/**
 * Helper pour récupérer l'email de l'utilisateur connecté
 * peu importe la méthode d'authentification (classique ou OAuth2)
 */
@Component
public class AuthenticationHelper {

    /**
     * Extrait l'email depuis l'objet Authentication
     *
     * @param authentication L'objet Spring Security contenant l'utilisateur connecté
     * @return L'email de l'utilisateur, ou null si non trouvé
     */
    public String getEmailFromAuthentication(Authentication authentication) {

        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        // ✅ CAS 1 : Authentification OAuth2 avec votre CustomOAuth2User
        if (principal instanceof CustomOAuth2User) {
            CustomOAuth2User oauth2User = (CustomOAuth2User) principal;
            return oauth2User.getUser().getEmail();
        }

        // ✅ CAS 2 : Authentification OAuth2 standard (fallback)
        if (principal instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) principal;
            return (String) oauth2User.getAttributes().get("email");
        }

        // ✅ CAS 3 : Authentification classique avec UserDetails
        if (principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) principal;
            return userDetails.getUsername(); // L'email est stocké comme username
        }

        // ✅ CAS 4 : Fallback si le principal est directement l'email (rare)
        if (principal instanceof String) {
            return (String) principal;
        }

        return null;
    }

    /**
     * Version simplifiée : lance une exception si l'email n'est pas trouvé
     */
    public String getEmailOrThrow(Authentication authentication) {
        String email = getEmailFromAuthentication(authentication);

        if (email == null) {
            throw new IllegalStateException("Impossible de récupérer l'email de l'utilisateur connecté");
        }

        return email;
    }
}