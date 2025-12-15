package com.ville.gestionincidents.service.utilisateur;

import com.ville.gestionincidents.dto.auth.RegisterDto;
import com.ville.gestionincidents.dto.utilisateur.citoyen.ChangePasswordDto;
import com.ville.gestionincidents.dto.utilisateur.citoyen.CitoyenProfilDto;
import com.ville.gestionincidents.dto.utilisateur.citoyen.CitoyenUpdateProfilDto;
import com.ville.gestionincidents.dto.utilisateur.superAdmin.CreateUtilisateurByAdminDto;
import com.ville.gestionincidents.dto.utilisateur.superAdmin.UpdateUtilisateurByAdminDto;
import com.ville.gestionincidents.entity.Departement;
import com.ville.gestionincidents.entity.Utilisateur;
import com.ville.gestionincidents.entity.ServiceMunicipal;
import com.ville.gestionincidents.enumeration.Role;
import com.ville.gestionincidents.mapper.UtilisateurMapper;
import com.ville.gestionincidents.repository.DepartementRepository;
import com.ville.gestionincidents.repository.ServiceMunicipalRepository;
import com.ville.gestionincidents.repository.UtilisateurRepository;
import com.ville.gestionincidents.service.email.EmailService;
import com.ville.gestionincidents.service.password.PasswordGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final ServiceMunicipalRepository serviceMunicipalRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final UtilisateurMapper utilisateurMapper;
    private final PasswordGeneratorService passwordGeneratorService;
    @Autowired
    private DepartementRepository departementRepository;

    // ==================== INSCRIPTION CITOYEN  ====================

    @Override
    @Transactional
    public boolean register(RegisterDto dto) {

        // 1. Vérifier si l'email existe déjà
        if (utilisateurRepository.findByEmail(dto.getEmail()).isPresent()) {
            return false;
        }

        // 2. Valider le mot de passe
        if (!isPasswordValid(dto.getMotDePasse())) {
            return false;
        }

        // 3. Vérifier que les mots de passe correspondent
        if (!dto.getMotDePasse().equals(dto.getConfirmMotDePasse())) {
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

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi de l'email : " + e.getMessage());
        }

        return true;
    }


    @Override
    @Transactional
    public boolean verifyEmail(String token) {

        Utilisateur utilisateur = utilisateurRepository.findByVerificationToken(token)
                .orElse(null);

        if (utilisateur == null) {
            return false;
        }

        // Vérifier si le token a expiré
        if (utilisateur.getVerificationTokenExpiration().isBefore(LocalDateTime.now())) {
            return false;
        }

        // Activer le compte
        utilisateur.setEmailVerifie(true);
        utilisateur.setVerificationToken(null);
        utilisateur.setVerificationTokenExpiration(null);

        utilisateurRepository.save(utilisateur);

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

    // ==================== CRÉATION PAR SUPERADMIN  ====================

    @Override
    @Transactional
    public Utilisateur createUserByAdmin(CreateUtilisateurByAdminDto dto, Role role) {
        // 0. Autogénérer mdp
        String temporaryPassword = passwordGeneratorService.generatePassword();
        // 1. Vérifier si l'email existe déjà
        if (utilisateurRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Cet email est déjà utilisé");
        }

        // 2. Créer l'utilisateur à partir du DTO
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNom(dto.getNom());
        utilisateur.setPrenom(dto.getPrenom());
        utilisateur.setEmail(dto.getEmail());
        // 3. Hasher le mot de passe autogénérée
        utilisateur.setMotDePasse(passwordEncoder.encode(temporaryPassword));
        // 4. Définir le rôle (ADMIN ou AGENT selon le paramètre)
        utilisateur.setRole(role);

        // 5. Email vérifié automatiquement pour les utilisateurs créés par admin
        utilisateur.setEmailVerifie(true);
        utilisateur.setVerificationToken(null);
        utilisateur.setVerificationTokenExpiration(null);

        if (dto.getDepartementId() != null) {
            Departement departement = departementRepository.findById(dto.getDepartementId())
                    .orElseThrow(() -> new RuntimeException("Département introuvable"));
            utilisateur.setDepartement(departement);
        }
        // 6. Assigner le service si un serviceId est fourni (pour les agents)
        if (dto.getServiceId() != null) {
            try {
                ServiceMunicipal service = serviceMunicipalRepository.findById(dto.getServiceId())
                        .orElseThrow(() -> new RuntimeException("Service introuvable"));
                utilisateur.setServiceMunicipal(service);
                System.out.println("✅ Agent assigné au service : " + service.getNom());
            } catch (Exception e) {
                System.err.println("⚠️ Erreur lors de l'assignation du service : " + e.getMessage());
                // On continue sans service si erreur
            }
        }

        // 7. Sauvegarder l'utilisateur
        Utilisateur savedUser = utilisateurRepository.save(utilisateur);

        // 8. ENVOYER UN EMAIL DE BIENVENUE (C'EST ICI QUE ÇA MANQUAIT !)
        try {
            emailService.sendWelcomeEmail(
                    savedUser.getEmail(),
                    savedUser.getNom(),
                    savedUser.getRole(),
                    temporaryPassword
            );
        } catch (Exception e) {
            System.err.println("⚠️ Utilisateur créé mais email non envoyé : " + e.getMessage());
            // L'utilisateur est créé même si l'email échoue
        }

        return savedUser;
    }

    // ==================== MODIFICATION PAR SUPERADMIN  ====================

    @Override
    @Transactional
    public Utilisateur updateUserByAdmin(Long id, UpdateUtilisateurByAdminDto dto) {

        // 1. Récupérer l'utilisateur existant
        Utilisateur existingUser = findById(id);

        // 2. Empêcher la modification d'un SUPERADMIN
        if (existingUser.getRole() == Role.SUPERADMIN) {
            throw new RuntimeException("Impossible de modifier un SUPERADMIN");
        }

        // 4. Vérifier si l'email a changé et s'il est disponible
        if (!existingUser.getEmail().equals(dto.getEmail())) {
            if (utilisateurRepository.findByEmail(dto.getEmail()).isPresent()) {
                throw new RuntimeException("Cet email est déjà utilisé");
            }
        }

        // Le mapper s'occupe de copier tous les champs du DTO vers l'entité
        utilisateurMapper.updateEntityFromDto(existingUser, dto);

        // 6. Sauvegarder les modifications
        Utilisateur updated = utilisateurRepository.save(existingUser);
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

    @Override
    public List<Utilisateur> findAgentsByService(Long serviceId) {
        return utilisateurRepository
                .findByServiceMunicipalIdAndRole(serviceId, Role.AGENT);
    }

    @Override
    public long countAgentsByDepartement(Departement departement) {
        return utilisateurRepository
                .countByDepartementAndRole(departement, Role.AGENT);
    }

}