package com.ville.gestionincidents.controller;

import com.ville.gestionincidents.dto.departement.DepartementDto;
import com.ville.gestionincidents.entity.Departement;
import com.ville.gestionincidents.mapper.DepartementMapper;
import com.ville.gestionincidents.mapper.ServiceMunicipalMapper;
import com.ville.gestionincidents.service.departement.DepartementService;
import com.ville.gestionincidents.service.serviceMunicipal.ServiceMunicipalService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequestMapping("/superadmin/departements")
@PreAuthorize("hasRole('SUPERADMIN')")
@RequiredArgsConstructor
public class DepartementController {

    private final DepartementService departementService;
    private final ServiceMunicipalService serviceMunicipalService;
    private final DepartementMapper departementMapper;
    private final ServiceMunicipalMapper serviceMunicipalMapper;

    // ==================== GESTION DES DÉPARTEMENTS ====================

    /**
     * Liste de tous les départements
     */
    @GetMapping
    public String listDepartements(Model model) {
        model.addAttribute("departements", departementMapper.toDtoList(departementService.findAll()));
        model.addAttribute("totalDepartements", departementService.countDepartements());
        model.addAttribute("totalServices", serviceMunicipalService.countServices());
        return "superadmin/departements/list";
    }

    /**
     * Formulaire de création de département
     */
    @GetMapping("/create")
    public String createDepartementForm(Model model) {
        model.addAttribute("departement", new DepartementDto());
        return "superadmin/departements/create";
    }

    /**
     * Créer un nouveau département
     */
    @PostMapping("/create")
    public String createDepartement(@Valid @ModelAttribute("departement") DepartementDto departementDto,
                                    BindingResult result,
                                    RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "superadmin/departements/create";
        }

        try {
            Departement departement = departementMapper.toEntity(departementDto);
            departementService.createDepartement(departement);
            redirectAttributes.addFlashAttribute("success",
                    "Département '" + departementDto.getNom() + "' créé avec succès");
            return "redirect:/superadmin/departements";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/superadmin/departements/create";
        }
    }

    /**
     * Voir détails d'un département avec ses services
     */
    @GetMapping("/{id}")
    public String viewDepartement(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Departement departement = departementService.findById(id);
            model.addAttribute("departement", departementMapper.toDto(departement));
            model.addAttribute("services", serviceMunicipalMapper.toDtoList(
                    serviceMunicipalService.findServicesByDepartement(id)));
            return "superadmin/departements/details";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Département introuvable");
            return "redirect:/superadmin/departements";
        }
    }

    /**
     * Formulaire de modification de département
     */
    @GetMapping("/{id}/edit")
    public String editDepartementForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Departement departement = departementService.findById(id);
            model.addAttribute("departement", departementMapper.toDto(departement));
            return "superadmin/departements/edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Département introuvable");
            return "redirect:/superadmin/departements";
        }
    }

    /**
     * Modifier un département
     */
    @PostMapping("/{id}/edit")
    public String editDepartement(@PathVariable Long id,
                                  @Valid @ModelAttribute("departement") DepartementDto departementDto,
                                  BindingResult result,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            departementDto.setId(id);
            model.addAttribute("departement", departementDto);
            return "superadmin/departements/edit";
        }

        try {
            Departement departement = departementMapper.toEntity(departementDto);
            departementService.updateDepartement(id, departement);
            redirectAttributes.addFlashAttribute("success", "Département modifié avec succès");
            return "redirect:/superadmin/departements/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/superadmin/departements/" + id + "/edit";
        }
    }

    /**
     * Supprimer un département
     */
    @PostMapping("/{id}/delete")
    public String deleteDepartement(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            departementService.deleteDepartement(id);
            redirectAttributes.addFlashAttribute("success", "Département supprimé avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/superadmin/departements";
    }
}