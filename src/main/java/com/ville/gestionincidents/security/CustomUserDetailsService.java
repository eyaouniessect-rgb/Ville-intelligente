package com.ville.gestionincidents.security;

import com.ville.gestionincidents.entity.Utilisateur;
import com.ville.gestionincidents.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
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
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        System.out.println("✅ Utilisateur trouvé : " + u.getEmail() + " | Role : " + u.getRole());

        return User.withUsername(u.getEmail())
                .password(u.getMotDePasse())
                .roles(u.getRole().name())
                .build();
    }
}
