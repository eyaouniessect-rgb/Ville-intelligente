package com.ville.gestionincidents.controller;

import com.ville.gestionincidents.dto.auth.RegisterDto;
import com.ville.gestionincidents.service.utilisateur.UtilisateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

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
    public String register(RegisterDto dto, Model model) {

        boolean ok = utilisateurService.register(dto);

        if (!ok) {
            model.addAttribute("error", "Email déjà utilisé !");
            return "auth/register";
        }

        return "redirect:/auth/login?success";
    }
}
