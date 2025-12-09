package com.ville.gestionincidents.service.utilisateur;

import com.ville.gestionincidents.dto.auth.RegisterDto;
import com.ville.gestionincidents.entity.Utilisateur;
import com.ville.gestionincidents.enumeration.Role;
import com.ville.gestionincidents.repository.UtilisateurRepository;
import com.ville.gestionincidents.service.email.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UtilisateurServiceImpl implements UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    @Transactional
    public boolean register(RegisterDto dto) {

        System.out.println("📝 Tentative d'inscription : " + dto.getEmail());

        // 1. Vérifier si l'email existe déjà
        if (utilisateurRepository.findByEmail(dto.getEmail()).isPresent()) {
            System.out.println("❌ Email déjà utilisé : " + dto.getEmail());
            return false;
        }

        // 2. Valider le mot de passe
        if (!isPasswordValid(dto.getMotDePasse())) {
            System.out.println("❌ Mot de passe invalide (ne respecte pas les critères de sécurité)");
            return false;
        }

        // 3. Vérifier que les mots de passe correspondent
        if (!dto.getMotDePasse().equals(dto.getConfirmMotDePasse())) {
            System.out.println("❌ Les mots de passe ne correspondent pas");
            return false;
        }

        // 4. Générer le token de vérification
        String token = UUID.randomUUID().toString();

        // 5. Créer l'utilisateur
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNom(dto.getNom());
        utilisateur.setPrenom(dto.getPrenom());
        utilisateur.setEmail(dto.getEmail());
        utilisateur.setMotDePasse(passwordEncoder.encode(dto.getMotDePasse()));
        utilisateur.setRole(Role.CITOYEN);
        utilisateur.setEmailVerifie(false); // ✅ Compte NON vérifié par défaut
        utilisateur.setVerificationToken(token);
        utilisateur.setVerificationTokenExpiration(LocalDateTime.now().plusHours(24)); // Expire dans 24h

        utilisateurRepository.save(utilisateur);

        // 6. Envoyer l'email de vérification
        try {
            emailService.sendVerificationEmail(utilisateur.getEmail(), token);
            System.out.println("✅ Utilisateur créé avec succès : " + utilisateur.getEmail());
            System.out.println("   Rôle : " + utilisateur.getRole());
            System.out.println("   📧 Email de vérification envoyé");
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi de l'email : " + e.getMessage());
            // L'utilisateur est créé mais l'email n'a pas pu être envoyé
        }

        return true;
    }

    // ✅ NOUVELLE MÉTHODE : Vérifier l'email
    @Override
    @Transactional
    public boolean verifyEmail(String token) {
        System.out.println("🔍 Tentative de vérification avec token : " + token);

        Utilisateur utilisateur = utilisateurRepository.findByVerificationToken(token)
                .orElse(null);

        if (utilisateur == null) {
            System.out.println("❌ Token invalide");
            return false;
        }

        // Vérifier si le token a expiré
        if (utilisateur.getVerificationTokenExpiration().isBefore(LocalDateTime.now())) {
            System.out.println("❌ Token expiré pour : " + utilisateur.getEmail());
            return false;
        }

        // Activer le compte
        utilisateur.setEmailVerifie(true);
        utilisateur.setVerificationToken(null);
        utilisateur.setVerificationTokenExpiration(null);

        utilisateurRepository.save(utilisateur);

        System.out.println("✅ Email vérifié avec succès pour : " + utilisateur.getEmail());
        return true;
    }

    /**
     * Valide qu'un mot de passe respecte les critères de sécurité
     */
    private boolean isPasswordValid(String password) {
        if (password == null || password.length() < 12) {
            System.out.println("   ❌ Mot de passe trop court : " + (password != null ? password.length() : 0) + " caractères (minimum 12)");
            return false;
        }

        boolean hasUppercase = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLowercase = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = password.matches(".*[@$!%*?&].*");

        if (!hasUppercase) System.out.println("   ❌ Aucune majuscule trouvée");
        if (!hasLowercase) System.out.println("   ❌ Aucune minuscule trouvée");
        if (!hasDigit) System.out.println("   ❌ Aucun chiffre trouvé");
        if (!hasSpecial) System.out.println("   ❌ Aucun caractère spécial trouvé (@$!%*?&)");

        return hasUppercase && hasLowercase && hasDigit && hasSpecial;
    }
}