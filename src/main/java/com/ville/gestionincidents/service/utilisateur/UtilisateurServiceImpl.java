package com.ville.gestionincidents.service.utilisateur;

import com.ville.gestionincidents.dto.auth.RegisterDto;
import com.ville.gestionincidents.dto.utilisateur.citoyen.ChangePasswordDto;
import com.ville.gestionincidents.dto.utilisateur.citoyen.CitoyenProfilDto;
import com.ville.gestionincidents.dto.utilisateur.citoyen.CitoyenUpdateProfilDto;
import com.ville.gestionincidents.dto.utilisateur.superAdmin.CreateUtilisateurByAdminDto;
import com.ville.gestionincidents.dto.utilisateur.superAdmin.UpdateUtilisateurByAdminDto;
import com.ville.gestionincidents.entity.Departement;
import com.ville.gestionincidents.entity.Utilisateur;
import com.ville.gestionincidents.enumeration.Role;
import com.ville.gestionincidents.mapper.UtilisateurMapper;
import com.ville.gestionincidents.repository.DepartementRepository;
import com.ville.gestionincidents.repository.UtilisateurRepository;
import com.ville.gestionincidents.service.email.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * ✅ IMPLÉMENTATION REFACTORISÉE DU SERVICE UTILISATEUR
 *
 * Changements principaux :
 * - Injection de UtilisateurMapper pour gérer les conversions DTO ↔ Entité
 * - createUserByAdmin() et updateUserByAdmin() utilisent maintenant des DTOs
 * - Meilleure séparation des responsabilités
 * - Validation renforcée des mots de passe
 */
@Service
@RequiredArgsConstructor
public class UtilisateurServiceImpl implements UtilisateurService {

    // ==================== DÉPENDANCES ====================

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final UtilisateurMapper utilisateurMapper; // ✅ NOUVEAU : Mapper pour conversions
    @Autowired
    private DepartementRepository departementRepository;

    // ==================== INSCRIPTION CITOYEN (INCHANGÉ) ====================

    /**
     * Inscrit un nouveau citoyen avec vérification par email
     * Le compte est créé mais désactivé jusqu'à vérification de l'email
     */
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

        // 4. Générer le token de vérification (valide 24h)
        String token = UUID.randomUUID().toString();

        // 5. Créer l'utilisateur via le MAPPER
        Utilisateur utilisateur = utilisateurMapper.toEntity(dto);
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

    /**
     * Vérifie l'email d'un utilisateur et active son compte
     */
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
//======================profil citoyen========
@Override
public CitoyenProfilDto getProfilCitoyen(String email) {
    Utilisateur user = findByEmail(email);
    return utilisateurMapper.toCitoyenProfilDto(user);
}

    @Override
    @Transactional
    public void updateProfilCitoyen(String email,
                                    CitoyenUpdateProfilDto dto) {

        Utilisateur user = findByEmail(email);

        if (user.getRole() != Role.CITOYEN) {
            throw new RuntimeException("Accès non autorisé");
        }

        utilisateurMapper.updateCitoyenProfil(user, dto);
        utilisateurRepository.save(user);
    }

    // ==================== CRÉATION PAR SUPERADMIN (✅ REFACTORISÉ AVEC DTO) ====================

    /**
     * ✅ REFACTORISÉ : Crée un utilisateur (ADMIN/AGENT) via DTO
     *
     * AVANTAGES DU DTO :
     * - Validation automatique des champs (@Valid dans le controller)
     * - Pas de risque d'injection de données non souhaitées
     * - Code plus propre et maintenable
     *
     * @param dto Données du formulaire de création
     * @param role Rôle à attribuer (ADMIN ou AGENT)
     * @return L'utilisateur créé
     */
    @Override
    @Transactional
    public Utilisateur createUserByAdmin(CreateUtilisateurByAdminDto dto, Role role) {

        if (utilisateurRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email déjà utilisé");
        }

        Utilisateur user = new Utilisateur();
        user.setNom(dto.getNom());
        user.setPrenom(dto.getPrenom());
        user.setEmail(dto.getEmail());
        user.setMotDePasse(passwordEncoder.encode(dto.getMotDePasse()));
        user.setRole(role);
        user.setEmailVerifie(true);

        // ✅ Association au département (ADMIN & AGENT)
        if (role == Role.ADMIN || role == Role.AGENT) {

            Departement departement = departementRepository
                    .findById(dto.getDepartementId())
                    .orElseThrow(() ->
                            new IllegalArgumentException("Département introuvable"));

            user.setDepartement(departement);
        }

        return utilisateurRepository.save(user); // ✅ retour
    }

    // ==================== MODIFICATION PAR SUPERADMIN (✅ REFACTORISÉ AVEC DTO) ====================

    /**
     * ✅ REFACTORISÉ : Met à jour un utilisateur via DTO
     *
     * IMPORTANT : Le mot de passe n'est PAS modifiable via cette méthode
     * Utilisez resetPasswordByAdmin() pour changer le mot de passe
     *
     * @param id ID de l'utilisateur à modifier
     * @param dto Nouvelles données (nom, prénom, email, rôle)
     * @return L'utilisateur modifié
     */
    @Override
    @Transactional
    public Utilisateur updateUserByAdmin(Long id, UpdateUtilisateurByAdminDto dto) {
        System.out.println("✏️ Modification d'utilisateur #" + id);

        // 1. Récupérer l'utilisateur existant
        Utilisateur existingUser = findById(id);

        // 2. Empêcher la modification d'un SUPERADMIN
        if (existingUser.getRole() == Role.SUPERADMIN) {
            System.out.println("❌ Tentative de modification d'un SUPERADMIN refusée");
            throw new RuntimeException("Impossible de modifier un SUPERADMIN");
        }

        // 3. Empêcher de promouvoir quelqu'un en SUPERADMIN
        if (dto.getRole() == Role.SUPERADMIN) {
            System.out.println("❌ Tentative de promotion en SUPERADMIN refusée");
            throw new RuntimeException("Impossible de promouvoir un utilisateur en SUPERADMIN");
        }

        // 4. Vérifier si l'email a changé et s'il est disponible
        if (!existingUser.getEmail().equals(dto.getEmail())) {
            if (utilisateurRepository.findByEmail(dto.getEmail()).isPresent()) {
                System.out.println("❌ Le nouvel email est déjà utilisé");
                throw new RuntimeException("Cet email est déjà utilisé");
            }
        }

        // 5. ✅ Mettre à jour l'entité via le MAPPER
        // Le mapper s'occupe de copier tous les champs du DTO vers l'entité
        utilisateurMapper.updateEntityFromDto(existingUser, dto);

        // 6. Sauvegarder les modifications
        Utilisateur updated = utilisateurRepository.save(existingUser);
        System.out.println("✅ Utilisateur modifié avec succès");
        return updated;
    }

    // ==================== RÉINITIALISATION MOT DE PASSE ====================

    /**
     * Réinitialise le mot de passe d'un utilisateur
     * Seule méthode pour changer le mot de passe (sécurité)
     */
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
        if (!isPasswordValid(newPassword)) {
            throw new RuntimeException("Le mot de passe ne respecte pas les critères de sécurité : " +
                    "12 caractères minimum, majuscule, minuscule, chiffre et caractère spécial.");
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

    // ==================== ACTIVATION/DÉSACTIVATION ====================

    /**
     * Active ou désactive un utilisateur (toggle)
     */
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

    // ==================== SUPPRESSION ====================

    /**
     * Supprime définitivement un utilisateur
     */
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

    // ==================== RECHERCHE D'UTILISATEURS ====================

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
    public List<Utilisateur> findAll() {
        return utilisateurRepository.findAll();
    }

    @Override
    public List<Utilisateur> findAllExceptSuperAdmin() {
        return utilisateurRepository.findAll().stream()
                .filter(u -> u.getRole() != Role.SUPERADMIN)
                .collect(Collectors.toList());
    }

    @Override
    public List<Utilisateur> findByRole(Role role) {
        return utilisateurRepository.findByRole(role);
    }

    @Override
    public List<Utilisateur> findRecentUsers(int limit) {
        return utilisateurRepository.findAll().stream()
                .sorted((u1, u2) -> {
                    // Trier par ID décroissant (plus récents en premier)
                    if (u2.getId() == null) return -1;
                    if (u1.getId() == null) return 1;
                    return u2.getId().compareTo(u1.getId());
                })
                .limit(limit)
                .collect(Collectors.toList());
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

    // ==================== MÉTHODE PRIVÉE DE VALIDATION ====================

    /**
     * Valide qu'un mot de passe respecte les critères de sécurité
     *
     * CRITÈRES :
     * - Minimum 12 caractères
     * - Au moins une majuscule (A-Z)
     * - Au moins une minuscule (a-z)
     * - Au moins un chiffre (0-9)
     * - Au moins un caractère spécial (@$!%*?&)
     *
     * @param password Le mot de passe à valider
     * @return true si valide, false sinon
     */
    private boolean isPasswordValid(String password) {
        if (password == null || password.length() < 12) {
            System.out.println("   ❌ Mot de passe trop court : " +
                    (password != null ? password.length() : 0) + " caractères (minimum 12)");
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
    @Override
    @Transactional
    public void changePasswordCitoyen(Long userId, ChangePasswordDto dto) {

        Utilisateur user = findById(userId);

        // 1️⃣ Vérifier ancien mot de passe
        if (!passwordEncoder.matches(
                dto.getAncienMotDePasse(),
                user.getMotDePasse())) {
            throw new RuntimeException("Ancien mot de passe incorrect");
        }

        // 2️⃣ Vérifier confirmation
        if (!dto.getNouveauMotDePasse()
                .equals(dto.getConfirmationMotDePasse())) {
            throw new RuntimeException("Les mots de passe ne correspondent pas");
        }

        // 3️⃣ Encoder et sauvegarder
        user.setMotDePasse(
                passwordEncoder.encode(dto.getNouveauMotDePasse())
        );

        utilisateurRepository.save(user);
    }

}