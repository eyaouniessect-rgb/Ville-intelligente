package com.ville.gestionincidents.security;

import com.ville.gestionincidents.entity.Utilisateur;
import com.ville.gestionincidents.enumeration.AuthProvider;
import com.ville.gestionincidents.enumeration.Role;
import com.ville.gestionincidents.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UtilisateurRepository utilisateurRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        return processOAuth2User(userRequest, oAuth2User);
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // Extraction des informations selon le provider
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, oAuth2User.getAttributes());

        if (userInfo.getEmail() == null || userInfo.getEmail().isEmpty()) {
            throw new OAuth2AuthenticationException("Email non trouvé dans les informations OAuth2");
        }

        Optional<Utilisateur> userOptional = utilisateurRepository.findByEmail(userInfo.getEmail());
        Utilisateur user;

        if (userOptional.isPresent()) {
            user = userOptional.get();

            // Vérifier que l'utilisateur utilise le même provider
            if (!user.getAuthProvider().equals(AuthProvider.valueOf(registrationId.toUpperCase()))) {
                throw new OAuth2AuthenticationException(
                        "Vous êtes déjà inscrit avec " + user.getAuthProvider() +
                                ". Veuillez utiliser la même méthode de connexion."
                );
            }

            // Mettre à jour les informations
            user = updateExistingUser(user, userInfo);
        } else {
            // Créer un nouvel utilisateur
            user = registerNewUser(userRequest, userInfo);
        }

        return new CustomOAuth2User(user, oAuth2User.getAttributes());
    }

    private Utilisateur registerNewUser(OAuth2UserRequest userRequest, OAuth2UserInfo userInfo) {
        Utilisateur user = new Utilisateur();

        user.setAuthProvider(AuthProvider.valueOf(userRequest.getClientRegistration().getRegistrationId().toUpperCase()));
        user.setProviderId(userInfo.getId());
        user.setEmail(userInfo.getEmail());
        user.setNom(userInfo.getLastName() != null ? userInfo.getLastName() : "");
        user.setPrenom(userInfo.getFirstName() != null ? userInfo.getFirstName() : userInfo.getName());
        user.setImageUrl(userInfo.getImageUrl());
        user.setRole(Role.CITOYEN); // Par défaut
        user.setEmailVerifie(true); // Auto-activé pour OAuth2

        return utilisateurRepository.save(user);
    }

    private Utilisateur updateExistingUser(Utilisateur user, OAuth2UserInfo userInfo) {
        if (userInfo.getLastName() != null) {
            user.setNom(userInfo.getLastName());
        }
        if (userInfo.getFirstName() != null) {
            user.setPrenom(userInfo.getFirstName());
        }
        user.setImageUrl(userInfo.getImageUrl());

        return utilisateurRepository.save(user);
    }
}