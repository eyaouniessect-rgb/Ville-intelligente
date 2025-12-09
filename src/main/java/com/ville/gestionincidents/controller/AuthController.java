package com.ville.gestionincidents.controller;

import com.ville.gestionincidents.dto.auth.RegisterDto;
import com.ville.gestionincidents.service.utilisateur.UtilisateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UtilisateurService utilisateurService;

    @GetMapping("/auth/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/auth/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new RegisterDto());
        return "auth/register";
    }

    @PostMapping("/auth/register")
    public String register(@Valid @ModelAttribute("user") RegisterDto dto,
                           BindingResult result,
                           Model model) {

        // 1. Vérifier les erreurs de validation (annotations @NotBlank, @Pattern, etc.)
        if (result.hasErrors()) {
            System.out.println("❌ Erreurs de validation détectées :");
            result.getAllErrors().forEach(error ->
                    System.out.println("  - " + error.getDefaultMessage())
            );
            return "auth/register";
        }

        // 2. Vérifier que les mots de passe correspondent
        if (!dto.getMotDePasse().equals(dto.getConfirmMotDePasse())) {
            model.addAttribute("error", "❌ Les mots de passe ne correspondent pas");
            return "auth/register";
        }

        // 3. Tenter l'inscription via le service
        boolean success = utilisateurService.register(dto);

        if (!success) {
            model.addAttribute("error", "❌ Cet email est déjà utilisé");
            return "auth/register";
        }

        // 4. Redirection vers login avec message de succès
        return "redirect:/auth/login?registered=true";
    }
    @GetMapping("/auth/verify")
    public String verifyEmail(@RequestParam String token, Model model) {
        boolean verified = utilisateurService.verifyEmail(token);

        if (verified) {
            return "redirect:/auth/login?verified=true";
        } else {
            model.addAttribute("error", "❌ Le lien de vérification est invalide ou a expiré.");
            return "redirect:/auth/login?error=invalid_token";
        }
    }
}