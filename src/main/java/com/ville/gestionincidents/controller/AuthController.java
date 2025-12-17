package com.ville.gestionincidents.controller;

/*
 * Import du DTO utilisé pour l’inscription.
 * Le DTO sert de contrat entre la vue (formulaire) et le backend,
 * sans exposer directement l’entité Utilisateur.
 */
import com.ville.gestionincidents.dto.auth.RegisterDto;

/*
 * Service métier responsable de la logique d’inscription,
 * de vérification d’email et de gestion des utilisateurs.
 */
import com.ville.gestionincidents.service.utilisateur.UtilisateurService;

/*
 * Lombok : génère automatiquement un constructeur
 * avec tous les attributs final (injection par constructeur).
 */
import lombok.RequiredArgsConstructor;

/*
 * Indique que cette classe est un contrôleur Spring MVC
 * qui retourne des vues Thymeleaf (et non du JSON).
 */
import org.springframework.stereotype.Controller;

/*
 * Model permet de transmettre des données du controller vers la vue.
 */
import org.springframework.ui.Model;

/*
 * Contient le résultat de la validation (@Valid).
 * Il doit toujours être placé juste après l’objet validé.
 */
import org.springframework.validation.BindingResult;

/*
 * Annotations Spring MVC pour le mapping des routes HTTP.
 */
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/*
 * Active la validation basée sur les annotations
 * (@NotBlank, @Email, @Size, etc.)
 */
import javax.validation.Valid;

/*
 * Contrôleur responsable de l’authentification :
 * - login
 * - register
 * - vérification d’email
 */
@Controller
@RequiredArgsConstructor
public class AuthController {

    /*
     * Injection du service utilisateur.
     * Le controller ne contient PAS de logique métier :
     * il délègue toujours au service.
     */
    private final UtilisateurService utilisateurService;

    /*
     * ======================
     * PAGE DE CONNEXION
     * ======================
     */
    @GetMapping("/auth/login")
    public String loginPage() {
        // Retourne la vue Thymeleaf : auth/login.html
        // Le traitement du login est géré par Spring Security
        return "auth/login";
    }

    /*
     * ======================
     * PAGE D’INSCRIPTION (GET)
     * ======================
     */
    @GetMapping("/auth/register")
    public String registerForm(Model model) {
        // Ajout d’un objet RegisterDto vide
        // pour le binding du formulaire Thymeleaf
        model.addAttribute("user", new RegisterDto());

        // Retourne la vue auth/register.html
        return "auth/register";
    }

    /*
     * ======================
     * INSCRIPTION (POST)
     * ======================
     */
    @PostMapping("/auth/register")
    public String register(
            // @Valid déclenche la validation du DTO
            @Valid @ModelAttribute("user") RegisterDto dto,

            // Contient les erreurs de validation s’il y en a
            BindingResult result,

            // Permet d’envoyer des messages à la vue
            Model model
    ) {

        /*
         * 1️⃣ Vérification des erreurs de validation
         * (ex: email invalide, champ vide, mot de passe trop court)
         */
        if (result.hasErrors()) {
            // Log console (utile en debug)
            System.out.println("❌ Erreurs de validation détectées :");
            result.getAllErrors().forEach(error ->
                    System.out.println("  - " + error.getDefaultMessage())
            );

            // Retour au formulaire sans appeler le service
            return "auth/register";
        }

        /*
         * 2️⃣ Vérification fonctionnelle :
         * comparaison mot de passe / confirmation
         */
        if (!dto.getMotDePasse().equals(dto.getConfirmMotDePasse())) {
            // Message d’erreur affiché dans la vue
            model.addAttribute("error", "❌ Les mots de passe ne correspondent pas");
            return "auth/register";
        }

        /*
         * 3️⃣ Tentative d’inscription via la couche service
         * Le service gère :
         * - vérification email unique
         * - encodage du mot de passe
         * - génération du token
         * - envoi du mail
         */
        boolean success = utilisateurService.register(dto);

        if (!success) {
            // Cas : email déjà existant
            model.addAttribute("error", "❌ Cet email est déjà utilisé");
            return "auth/register";
        }

        /*
         * 4️⃣ Succès :
         * redirection vers la page de login
         * avec un paramètre pour afficher un message
         */
        return "redirect:/auth/login?registered=true";
    }

    /*
     * ======================
     * VÉRIFICATION D’EMAIL
     * ======================
     */
    @GetMapping("/auth/verify")
    public String verifyEmail(
            // Token reçu depuis l’URL envoyée par email
            @RequestParam String token,

            // Utilisé pour afficher un message d’erreur si besoin
            Model model
    ) {
        // Appel au service pour valider le token
        boolean verified = utilisateurService.verifyEmail(token);

        if (verified) {
            // Email vérifié avec succès
            return "redirect:/auth/login?verified=true";
        } else {
            // Token invalide ou expiré
            model.addAttribute("error",
                    "❌ Le lien de vérification est invalide ou a expiré.");

            return "redirect:/auth/login?error=invalid_token";
        }
    }
}
