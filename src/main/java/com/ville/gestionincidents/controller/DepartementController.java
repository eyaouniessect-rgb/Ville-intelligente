package com.ville.gestionincidents.controller;

import com.ville.gestionincidents.entity.Departement;
import com.ville.gestionincidents.entity.ServiceMunicipal;
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

    // ==================== GESTION DES DÉPARTEMENTS ====================

    /**
     * Liste de tous les départements
     */
    @GetMapping
    public String listDepartements(Model model) {
        model.addAttribute("departements", departementService.findAll());
        model.addAttribute("totalDepartements", departementService.countDepartements());
        model.addAttribute("totalServices", serviceMunicipalService.countServices());
        return "superadmin/departements/list";
    }

    /**
     * Formulaire de création de département
     */
    @GetMapping("/create")
    public String createDepartementForm(Model model) {
        model.addAttribute("departement", new Departement());
        return "superadmin/departements/create";
    }

    /**
     * Créer un nouveau département
     */
    @PostMapping("/create")
    public String createDepartement(@Valid @ModelAttribute Departement departement,
                                    BindingResult result,
                                    RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "superadmin/departements/create";
        }

        try {
            departementService.createDepartement(departement);
            redirectAttributes.addFlashAttribute("success",
                    "Département '" + departement.getNom() + "' créé avec succès");
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
            model.addAttribute("departement", departement);
            model.addAttribute("services", serviceMunicipalService.findServicesByDepartement(id));
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
            model.addAttribute("departement", departementService.findById(id));
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
                                  @Valid @ModelAttribute Departement departement,
                                  BindingResult result,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            // Réinjecter le département avec l'ID pour éviter les erreurs
            departement.setId(id);
            model.addAttribute("departement", departement);
            return "superadmin/departements/edit";
        }

        try {
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

    // ==================== GESTION DES SERVICES ====================

    /**
     * Formulaire d'ajout de service à un département
     */
    @GetMapping("/{departementId}/services/add")
    public String addServiceForm(@PathVariable Long departementId, Model model, RedirectAttributes redirectAttributes) {
        try {
            Departement departement = departementService.findById(departementId);
            model.addAttribute("departement", departement);
            model.addAttribute("service", new ServiceMunicipal());
            return "superadmin/departements/add-service";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Département introuvable");
            return "redirect:/superadmin/departements";
        }
    }

    /**
     * Ajouter un service à un département
     */
    @PostMapping("/{departementId}/services/add")
    public String addService(@PathVariable Long departementId,
                             @Valid @ModelAttribute ServiceMunicipal service,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("departement", departementService.findById(departementId));
            return "superadmin/departements/add-service";
        }

        try {
            serviceMunicipalService.addServiceToDepartement(departementId, service);
            redirectAttributes.addFlashAttribute("success",
                    "Service '" + service.getNom() + "' ajouté avec succès");
            return "redirect:/superadmin/departements/" + departementId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/superadmin/departements/" + departementId + "/services/add";
        }
    }

    /**
     * Formulaire de modification d'un service
     */
    @GetMapping("/services/{serviceId}/edit")
    public String editServiceForm(@PathVariable Long serviceId, Model model, RedirectAttributes redirectAttributes) {
        try {
            ServiceMunicipal service = serviceMunicipalService.findServiceById(serviceId);
            model.addAttribute("service", service);
            model.addAttribute("departement", service.getDepartement());
            return "superadmin/departements/edit-service";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Service introuvable");
            return "redirect:/superadmin/departements";
        }
    }

    /**
     * Modifier un service
     */
    @PostMapping("/services/{serviceId}/edit")
    public String editService(@PathVariable Long serviceId,
                              @Valid @ModelAttribute ServiceMunicipal service,
                              BindingResult result,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            // Réinjecter le service et le département pour éviter les erreurs
            try {
                ServiceMunicipal existing = serviceMunicipalService.findServiceById(serviceId);
                service.setId(serviceId);
                service.setDepartement(existing.getDepartement());
                model.addAttribute("service", service);
                model.addAttribute("departement", existing.getDepartement());
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Service introuvable");
                return "redirect:/superadmin/departements";
            }
            return "superadmin/departements/edit-service";
        }

        try {
            ServiceMunicipal updated = serviceMunicipalService.updateService(serviceId, service);
            redirectAttributes.addFlashAttribute("success", "Service modifié avec succès");
            return "redirect:/superadmin/departements/" + updated.getDepartement().getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/superadmin/departements/services/" + serviceId + "/edit";
        }
    }

    /**
     * Supprimer un service
     */
    @PostMapping("/services/{serviceId}/delete")
    public String deleteService(@PathVariable Long serviceId, RedirectAttributes redirectAttributes) {
        try {
            ServiceMunicipal service = serviceMunicipalService.findServiceById(serviceId);
            Long departementId = service.getDepartement().getId();
            serviceMunicipalService.deleteService(serviceId);
            redirectAttributes.addFlashAttribute("success", "Service supprimé avec succès");
            return "redirect:/superadmin/departements/" + departementId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/superadmin/departements";
        }
    }
}