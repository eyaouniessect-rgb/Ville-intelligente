package com.ville.gestionincidents.service.email;

import com.ville.gestionincidents.enumeration.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    // ==================== MÉTHODES EXISTANTES ====================

    @Override
    public void sendVerificationEmail(String to, String token) {
        String verificationUrl = baseUrl + "/auth/verify?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("✅ Vérifiez votre compte - Ville Intelligente");
        message.setText(
                "Bonjour,\n\n" +
                        "Merci de vous être inscrit sur notre plateforme de gestion d'incidents.\n\n" +
                        "Pour activer votre compte, cliquez sur le lien ci-dessous :\n" +
                        verificationUrl + "\n\n" +
                        "Ce lien expire dans 24 heures.\n\n" +
                        "Si vous n'avez pas créé de compte, ignorez cet email.\n\n" +
                        "Cordialement,\n" +
                        "L'équipe Ville Intelligente"
        );

        try {
            mailSender.send(message);
            System.out.println("📧 Email de vérification envoyé à : " + to);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi de l'email : " + e.getMessage());
            throw new RuntimeException("Impossible d'envoyer l'email de vérification", e);
        }
    }

    @Override
    public void sendPasswordResetEmail(String to, String token) {
        String resetUrl = baseUrl + "/auth/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("🔐 Réinitialisation de votre mot de passe");
        message.setText(
                "Bonjour,\n\n" +
                        "Vous avez demandé à réinitialiser votre mot de passe.\n\n" +
                        "Cliquez sur le lien ci-dessous pour créer un nouveau mot de passe :\n" +
                        resetUrl + "\n\n" +
                        "Ce lien expire dans 1 heure.\n\n" +
                        "Si vous n'avez pas fait cette demande, ignorez cet email.\n\n" +
                        "Cordialement,\n" +
                        "L'équipe Ville Intelligente"
        );

        try {
            mailSender.send(message);
            System.out.println("📧 Email de réinitialisation envoyé à : " + to);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi de l'email de réinitialisation : " + e.getMessage());
        }
    }

    /**
     *  Envoie un email de bienvenue pour les utilisateurs créés par admin
     */
    @Override
    public void sendWelcomeEmail(String to, String nom, Role role, String password) {
        String roleLabel = getRoleLabel(role);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("👋 Bienvenue sur la plateforme - Ville Intelligente");
        message.setText(
                "Bonjour " + nom + ",\n\n" +
                        "Votre compte " + roleLabel + " a été créé avec succès sur notre plateforme de gestion d'incidents.\n\n" +
                        "Vos identifiants de connexion :\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                        "Email : " + to + "\n" +
                        "Mot de passe : " + password + "\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                        "Vous pouvez vous connecter à l'adresse suivante :\n" +
                        baseUrl + "/auth/login\n\n" +
                        "⚠️ IMPORTANT : Nous vous recommandons fortement de changer votre mot de passe lors de votre première connexion pour des raisons de sécurité.\n\n" +
                        "Si vous avez des questions, n'hésitez pas à contacter un administrateur.\n\n" +
                        "Cordialement,\n" +
                        "L'équipe Ville Intelligente"
        );

        try {
            mailSender.send(message);
            System.out.println("📧 Email de bienvenue envoyé à : " + to + " (Rôle : " + role + ")");
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi de l'email de bienvenue : " + e.getMessage());
            throw new RuntimeException("Impossible d'envoyer l'email de bienvenue", e);
        }
    }

    /**
     * Envoie une notification après réinitialisation du mot de passe par admin
     */
    @Override
    public void sendPasswordResetNotification(String to) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("🔐 Votre mot de passe a été réinitialisé");
        message.setText(
                "Bonjour,\n\n" +
                        "Votre mot de passe a été réinitialisé par un administrateur.\n\n" +
                        "Vous pouvez maintenant vous connecter avec votre nouveau mot de passe à l'adresse suivante :\n" +
                        baseUrl + "/auth/login\n\n" +
                        "⚠️ IMPORTANT :\n" +
                        "- Si vous n'êtes pas à l'origine de cette demande, contactez immédiatement un administrateur.\n" +
                        "- Nous vous recommandons de changer à nouveau votre mot de passe après votre première connexion.\n\n" +
                        "Cordialement,\n" +
                        "L'équipe Ville Intelligente"
        );

        try {
            mailSender.send(message);
            System.out.println("📧 Notification de réinitialisation envoyée à : " + to);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi de la notification : " + e.getMessage());
            throw new RuntimeException("Impossible d'envoyer la notification", e);
        }
    }


    @Override
    public void sendSimpleEmail(String to, String subject, String messageText) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(messageText);

        try {
            mailSender.send(message);
            System.out.println("📧 Email envoyé à : " + to + " | Sujet : " + subject);
        } catch (Exception e) {
            System.err.println("❌ Erreur envoi email simple : " + e.getMessage());
        }
    }


    // ==================== MÉTHODES UTILITAIRES ====================

    /**
     * Convertit le rôle en label français
     */
    private String getRoleLabel(Role role) {
        switch (role) {
            case SUPERADMIN:
                return "Super Administrateur";
            case ADMIN:
                return "Administrateur";
            case AGENT:
                return "Agent Municipal";
            case CITOYEN:
                return "Citoyen";
            default:
                return "Utilisateur";
        }
    }


}