package com.ville.gestionincidents.service.email;

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
    public void sendIncidentUpdateEmail(String to, Long incidentId, String nouveauStatut) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("🔔 Mise à jour de votre incident #" + incidentId);
        message.setText(
                "Bonjour,\n\n" +
                        "Votre incident #" + incidentId + " a été mis à jour.\n\n" +
                        "Nouveau statut : " + nouveauStatut + "\n\n" +
                        "Vous pouvez consulter les détails sur votre tableau de bord :\n" +
                        baseUrl + "/incidents/" + incidentId + "\n\n" +
                        "Cordialement,\n" +
                        "L'équipe Ville Intelligente"
        );

        try {
            mailSender.send(message);
            System.out.println("📧 Notification d'incident envoyée à : " + to);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi de la notification : " + e.getMessage());
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
}