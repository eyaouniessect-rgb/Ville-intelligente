package com.ville.gestionincidents.security;

import java.util.Map;

/**
 * Classe abstraite pour gérer les informations utilisateur OAuth2
 */
public abstract class OAuth2UserInfo {
    protected Map<String, Object> attributes;

    public OAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public abstract String getId();
    public abstract String getName();
    public abstract String getEmail();
    public abstract String getImageUrl();
    public abstract String getFirstName();
    public abstract String getLastName();
}