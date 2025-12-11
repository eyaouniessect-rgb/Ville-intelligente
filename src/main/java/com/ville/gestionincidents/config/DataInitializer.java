package com.ville.gestionincidents.config;

import com.ville.gestionincidents.entity.Utilisateur;
import com.ville.gestionincidents.enumeration.Role;
import com.ville.gestionincidents.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Initialise un compte SUPERADMIN au démarrage de l'application
 * si aucun SUPERADMIN n'existe encore.
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Vérifier si un SUPERADMIN existe déjà
        long superAdminCount = utilisateurRepository.countByRole(Role.SUPERADMIN);

        if (superAdminCount == 0) {
            System.out.println("==========================================");
            System.out.println("🔧 Initialisation du compte SUPERADMIN");
            System.out.println("==========================================");

            Utilisateur superAdmin = new Utilisateur();
            superAdmin.setNom("Admin");
            superAdmin.setPrenom("Super");
            superAdmin.setEmail("superadmin@ville.intelligente");
            // Mot de passe : SuperAdmin123!@#
            superAdmin.setMotDePasse(passwordEncoder.encode("SuperAdmin123!@#"));
            superAdmin.setRole(Role.SUPERADMIN);
            superAdmin.setEmailVerifie(true); // ✅ Compte activé directement

            utilisateurRepository.save(superAdmin);

            System.out.println("✅ SUPERADMIN créé avec succès !");
            System.out.println("📧 Email    : superadmin@ville.intelligente");
            System.out.println("🔑 Password : SuperAdmin123!@#");
            System.out.println("==========================================");
        } else {
            System.out.println("ℹ️  Un compte SUPERADMIN existe déjà. Aucune initialisation nécessaire.");
        }
    }
}

