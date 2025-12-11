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
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UtilisateurServiceImpl implements UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    // ==================== INSCRIPTION ET VÉRIFICATION (EXISTANT) ====================

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
        utilisateur.setVerificationTokenExpiration(LocalDateTime.now().plusHours(24));

        utilisateurRepository.save(utilisateur);

        // 6. Envoyer l'email de vérification
        try {
            emailService.sendVerificationEmail(utilisateur.getEmail(), token);
            System.out.println("✅ Utilisateur créé avec succès : " + utilisateur.getEmail());
            System.out.println("   Rôle : " + utilisateur.getRole());
            System.out.println("   📧 Email de vérification envoyé");
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi de l'email : " + e.getMessage());
        }

        return true;
    }

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

    // ==================== MÉTHODES POUR SUPERADMIN (NOUVEAU) ====================

    @Override
    @Transactional
    public Utilisateur createUserByAdmin(Utilisateur utilisateur) {
        System.out.println("👨‍💼 Création d'utilisateur par ADMIN : " + utilisateur.getEmail());

        // Vérifier si l'email existe déjà
        if (utilisateurRepository.findByEmail(utilisateur.getEmail()).isPresent()) {
            System.out.println("❌ Email déjà utilisé : " + utilisateur.getEmail());
            throw new RuntimeException("Cet email est déjà utilisé");
        }

        // Hasher le mot de passe
        utilisateur.setMotDePasse(passwordEncoder.encode(utilisateur.getMotDePasse()));

        // Email vérifié automatiquement pour les utilisateurs créés par admin
        utilisateur.setEmailVerifie(true);
        utilisateur.setVerificationToken(null);
        utilisateur.setVerificationTokenExpiration(null);

        // Sauvegarder l'utilisateur
        Utilisateur savedUser = utilisateurRepository.save(utilisateur);

        // Envoyer un email de bienvenue
        try {
            emailService.sendWelcomeEmail(
                    savedUser.getEmail(),
                    savedUser.getNom(),
                    savedUser.getRole()
            );
            System.out.println("✅ Utilisateur créé avec succès : " + savedUser.getEmail());
            System.out.println("   Rôle : " + savedUser.getRole());
            System.out.println("   📧 Email de bienvenue envoyé");
        } catch (Exception e) {
            System.err.println("⚠️ Utilisateur créé mais email non envoyé : " + e.getMessage());
        }

        return savedUser;
    }

    @Override
    @Transactional
    public Utilisateur updateUserByAdmin(Long id, Utilisateur utilisateur) {
        System.out.println("✏️ Modification d'utilisateur #" + id);

        Utilisateur existingUser = findById(id);

        // Empêcher la modification d'un SUPERADMIN
        if (existingUser.getRole() == Role.SUPERADMIN) {
            System.out.println("❌ Tentative de modification d'un SUPERADMIN refusée");
            throw new RuntimeException("Impossible de modifier un SUPERADMIN");
        }

        // Mise à jour des champs
        existingUser.setNom(utilisateur.getNom());
        existingUser.setPrenom(utilisateur.getPrenom());

        // Vérifier si l'email a changé
        if (!existingUser.getEmail().equals(utilisateur.getEmail())) {
            if (utilisateurRepository.findByEmail(utilisateur.getEmail()).isPresent()) {
                System.out.println("❌ Le nouvel email est déjà utilisé");
                throw new RuntimeException("Cet email est déjà utilisé");
            }
            existingUser.setEmail(utilisateur.getEmail());
        }

        // Mise à jour du rôle (sauf SUPERADMIN)
        if (utilisateur.getRole() != Role.SUPERADMIN) {
            existingUser.setRole(utilisateur.getRole());
        }

        Utilisateur updated = utilisateurRepository.save(existingUser);
        System.out.println("✅ Utilisateur modifié avec succès");
        return updated;
    }

    @Override
    public Utilisateur findById(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable avec l'ID : " + id));
    }

    @Override
    public Utilisateur findByEmail(String email) {
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable avec l'email : " + email));
    }

    @Override
    public List<Utilisateur> findAllExceptSuperAdmin() {
        return utilisateurRepository.findAll().stream()
                .filter(u -> u.getRole() != Role.SUPERADMIN)
                .collect(Collectors.toList());
    }

    @Override
    public List<Utilisateur> findAll() {
        return utilisateurRepository.findAll();
    }

    @Override
    public List<Utilisateur> findByRole(Role role) {
        return utilisateurRepository.findByRole(role);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        System.out.println("🗑️ Tentative de suppression d'utilisateur #" + id);

        Utilisateur user = findById(id);

        // Empêcher la suppression d'un SUPERADMIN
        if (user.getRole() == Role.SUPERADMIN) {
            System.out.println("❌ Tentative de suppression d'un SUPERADMIN refusée");
            throw new RuntimeException("Impossible de supprimer un SUPERADMIN");
        }

        utilisateurRepository.deleteById(id);
        System.out.println("✅ Utilisateur supprimé : " + user.getEmail());
    }

    @Override
    @Transactional
    public void toggleUserStatus(Long id) {
        System.out.println("🔄 Changement de statut pour utilisateur #" + id);

        Utilisateur user = findById(id);

        // Empêcher la désactivation d'un SUPERADMIN
        if (user.getRole() == Role.SUPERADMIN) {
            System.out.println("❌ Tentative de modification du statut d'un SUPERADMIN refusée");
            throw new RuntimeException("Impossible de modifier le statut d'un SUPERADMIN");
        }

        boolean newStatus = !user.isEmailVerifie();
        user.setEmailVerifie(newStatus);
        utilisateurRepository.save(user);

        System.out.println("✅ Statut modifié : " + (newStatus ? "Activé" : "Désactivé"));
    }

    @Override
    @Transactional
    public void resetPasswordByAdmin(Long id, String newPassword) {
        System.out.println("🔑 Réinitialisation du mot de passe pour utilisateur #" + id);

        Utilisateur user = findById(id);

        // Empêcher la modification du mot de passe d'un SUPERADMIN
        if (user.getRole() == Role.SUPERADMIN) {
            System.out.println("❌ Tentative de modification du mot de passe d'un SUPERADMIN refusée");
            throw new RuntimeException("Impossible de modifier le mot de passe d'un SUPERADMIN");
        }

        // Valider le nouveau mot de passe
        if (newPassword == null || newPassword.length() < 8) {
            throw new RuntimeException("Le mot de passe doit contenir au moins 8 caractères");
        }

        user.setMotDePasse(passwordEncoder.encode(newPassword));
        utilisateurRepository.save(user);

        // Envoyer un email de notification
        try {
            emailService.sendPasswordResetNotification(user.getEmail());
            System.out.println("✅ Mot de passe réinitialisé et notification envoyée");
        } catch (Exception e) {
            System.err.println("⚠️ Mot de passe réinitialisé mais email non envoyé : " + e.getMessage());
        }
    }

    // ==================== STATISTIQUES ====================

    @Override
    public long countAllUsers() {
        return utilisateurRepository.count();
    }

    @Override
    public long countByRole(Role role) {
        return utilisateurRepository.countByRole(role);
    }

    @Override
    public List<Utilisateur> findRecentUsers(int limit) {
        return utilisateurRepository.findAll().stream()
                .sorted((u1, u2) -> {
                    // Trier par ID décroissant (les plus récents en premier)
                    if (u2.getId() == null) return -1;
                    if (u1.getId() == null) return 1;
                    return u2.getId().compareTo(u1.getId());
                })
                .limit(limit)
                .collect(Collectors.toList());
    }

    // ==================== MÉTHODES PRIVÉES ====================

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