package com.ville.gestionincidents.security;

import com.ville.gestionincidents.entity.Utilisateur;
import com.ville.gestionincidents.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("🔍 Tentative de connexion pour : " + email);

        Utilisateur u = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> {
                    System.out.println("❌ Utilisateur non trouvé : " + email);
                    return new UsernameNotFoundException("Utilisateur non trouvé");
                });

        System.out.println("✅ Utilisateur trouvé : " + u.getEmail() + " | Rôle : " + u.getRole());

        // ✅ VÉRIFICATION : Bloquer si l'email n'est pas vérifié
        if (!u.isEmailVerifie()) {
            System.out.println("❌ Email non vérifié pour : " + u.getEmail());
            throw new DisabledException("EMAIL_NOT_VERIFIED");
        }

        System.out.println("✅ Email vérifié, connexion autorisée");

        return User.withUsername(u.getEmail())
                .password(u.getMotDePasse())
                .roles(u.getRole().name())
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false) // ✅ Le compte est actif si l'email est vérifié
                .build();
    }
}