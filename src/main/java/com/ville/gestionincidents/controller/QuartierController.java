package com.ville.gestionincidents.controller;

import com.ville.gestionincidents.entity.Quartier;
import com.ville.gestionincidents.service.quartier.QuartierService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequestMapping("/superadmin/quartiers")
@PreAuthorize("hasRole('SUPERADMIN')")
@RequiredArgsConstructor
public class QuartierController {

    private final QuartierService quartierService;

    // ==================== GESTION DES QUARTIERS ====================

    /**
     * Liste de tous les quartiers
     */
    @GetMapping
    public String listQuartiers(Model model) {
        model.addAttribute("quartiers", quartierService.findAll());
        model.addAttribute("totalQuartiers", quartierService.countQuartiers());
        return "superadmin/quartiers/list";
    }

    /**
     * Formulaire de création de quartier
     */
    @GetMapping("/create")
    public String createQuartierForm(Model model) {
        model.addAttribute("quartier", new Quartier());
        return "superadmin/quartiers/create";
    }

    /**
     * Créer un nouveau quartier
     */
    @PostMapping("/create")
    public String createQuartier(@Valid @ModelAttribute Quartier quartier,
                                 BindingResult result,
                                 RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "superadmin/quartiers/create";
        }

        try {
            quartierService.createQuartier(quartier);
            redirectAttributes.addFlashAttribute("success",
                    "Quartier '" + quartier.getNom() + "' créé avec succès");
            return "redirect:/superadmin/quartiers";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/superadmin/quartiers/create";
        }
    }

    /**
     * Voir détails d'un quartier
     */
    @GetMapping("/{id}")
    public String viewQuartier(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Quartier quartier = quartierService.findById(id);
            model.addAttribute("quartier", quartier);
            return "superadmin/quartiers/details";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Quartier introuvable");
            return "redirect:/superadmin/quartiers";
        }
    }

    /**
     * Formulaire de modification de quartier
     */
    @GetMapping("/{id}/edit")
    public String editQuartierForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("quartier", quartierService.findById(id));
            return "superadmin/quartiers/edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Quartier introuvable");
            return "redirect:/superadmin/quartiers";
        }
    }

    /**
     * Mettre à jour un quartier
     */
    @PostMapping("/{id}/edit")
    public String editQuartier(@PathVariable Long id,
                               @Valid @ModelAttribute Quartier quartier,
                               BindingResult result,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "superadmin/quartiers/edit";
        }

        try {
            quartierService.updateQuartier(id, quartier);
            redirectAttributes.addFlashAttribute("success",
                    "Quartier modifié avec succès");
            return "redirect:/superadmin/quartiers/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/superadmin/quartiers/" + id + "/edit";
        }
    }

    /**
     * Supprimer un quartier
     */
    @PostMapping("/{id}/delete")
    public String deleteQuartier(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            quartierService.deleteQuartier(id);
            redirectAttributes.addFlashAttribute("success",
                    "Quartier supprimé avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/superadmin/quartiers";
    }
}