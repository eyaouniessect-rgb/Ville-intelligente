package com.ville.gestionincidents.controller.citoyen;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/citoyen")
public class CitoyenController {

    @GetMapping("/home")
    public String homeCitoyen() {
        return "citoyen/home"; // le bon template HTML
    }
}
