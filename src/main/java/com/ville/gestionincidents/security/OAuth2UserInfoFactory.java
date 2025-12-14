package com.ville.gestionincidents.security;

import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

import java.util.Map;

/**
 * Factory pour créer le bon type d'OAuth2UserInfo selon le provider
 */
public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if (registrationId.equalsIgnoreCase("google")) {
            return new GoogleOAuth2UserInfo(attributes);
        } else {
            throw new OAuth2AuthenticationException(
                    "Connexion avec " + registrationId + " n'est pas supportée pour le moment"
            );
        }
    }
}