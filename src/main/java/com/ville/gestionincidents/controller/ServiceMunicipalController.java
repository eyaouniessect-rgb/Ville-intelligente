package com.ville.gestionincidents.controller;

import com.ville.gestionincidents.dto.service.ServiceMunicipalDto;
import com.ville.gestionincidents.entity.Departement;
import com.ville.gestionincidents.entity.ServiceMunicipal;
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
import java.util.List;

@Controller
@RequestMapping("/superadmin/services")
@PreAuthorize("hasRole('SUPERADMIN')")
@RequiredArgsConstructor
public class ServiceMunicipalController {

    private final ServiceMunicipalService serviceMunicipalService;
    private final DepartementService departementService;
    private final ServiceMunicipalMapper serviceMunicipalMapper;

    // ==================== LISTE DES SERVICES ====================

    /**
     * Liste de tous les services municipaux
     */
    @GetMapping
    public String listServices(Model model) {
        List<ServiceMunicipal> services = serviceMunicipalService.findAllServices();
        model.addAttribute("services", serviceMunicipalMapper.toDtoList(services));
        model.addAttribute("totalServices", serviceMunicipalService.countServices());
        return "superadmin/departements/list";
    }

    // ==================== CRÉATION DE SERVICE ====================

    /**
     * Formulaire de création de service
     */
    @GetMapping("/create")
    public String createServiceForm(@RequestParam Long departementId, Model model) {

        Departement departement = departementService.findById(departementId);

        ServiceMunicipalDto serviceDto = new ServiceMunicipalDto();
        serviceDto.setDepartementId(departementId);

        model.addAttribute("service", serviceDto);
        model.addAttribute("departement", departement);

        return "superadmin/departements/add-service";
    }

    /**
     * Créer un nouveau service
     */
    @PostMapping("/create")
    public String createService(@Valid @ModelAttribute("service") ServiceMunicipalDto serviceDto,
                                BindingResult result,
                                Model model,
                                RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            Departement departement =
                    departementService.findById(serviceDto.getDepartementId());

            model.addAttribute("departement", departement);
            return "superadmin/departements/add-service";
        }

        try {
            ServiceMunicipal service = serviceMunicipalMapper.toEntity(serviceDto);
            serviceMunicipalService.addServiceToDepartement(
                    serviceDto.getDepartementId(), service);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Service '" + serviceDto.getNom() + "' créé avec succès"
            );

            return "redirect:/superadmin/departements/" + serviceDto.getDepartementId();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/superadmin/services/create?departementId=" + serviceDto.getDepartementId();
        }
    }


    // ==================== DÉTAILS DU SERVICE ====================

    /**
     * Voir détails d'un service
     */
    @GetMapping("/{id}")
    public String viewService(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            ServiceMunicipal service = serviceMunicipalService.findServiceById(id);
            model.addAttribute("service", serviceMunicipalMapper.toDto(service));
            return "superadmin/departements/details";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Service introuvable");
            return "redirect:/superadmin/services";
        }
    }

    // ==================== MODIFICATION DE SERVICE ====================

    /**
     * Formulaire de modification de service
     */
    @GetMapping("/{id}/edit")
    public String editServiceForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            ServiceMunicipal service = serviceMunicipalService.findServiceById(id);
            model.addAttribute("service", serviceMunicipalMapper.toDto(service));
            model.addAttribute("departement", service.getDepartement());
            model.addAttribute("departements", departementService.findAll());
            return "superadmin/departements/edit-service";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Service introuvable");
            return "redirect:/superadmin/services";
        }
    }

    /**
     * Modifier un service
     */
    @PostMapping("/{id}/edit")
    public String editService(@PathVariable Long id,
                              @Valid @ModelAttribute("service") ServiceMunicipalDto serviceDto,
                              BindingResult result,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            serviceDto.setId(id);
            model.addAttribute("service", serviceDto);
            model.addAttribute("departements", departementService.findAll());
            return "superadmin/departements/edit-service";
        }

        try {
            ServiceMunicipal service = serviceMunicipalMapper.toEntity(serviceDto);
            ServiceMunicipal updated = serviceMunicipalService.updateService(id, service);

            redirectAttributes.addFlashAttribute("success", "Service modifié avec succès");

            Long departementId = updated.getDepartement().getId();
            return "redirect:/superadmin/departements/" + departementId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/superadmin/services/" + id + "/edit";
        }
    }

    // ==================== SUPPRESSION DE SERVICE ====================

    /**
     * Supprimer un service
     */
    @PostMapping("/{id}/delete")
    public String deleteService(@PathVariable Long id,
                                @RequestParam(required = false) Long departementId,
                                RedirectAttributes redirectAttributes) {
        try {
            ServiceMunicipal service = serviceMunicipalService.findServiceById(id);
            serviceMunicipalService.deleteService(id);

            redirectAttributes.addFlashAttribute("success", "Service supprimé avec succès");

            if (departementId != null) {
                return "redirect:/superadmin/departements/" + departementId;
            }
            return "redirect:/superadmin/services";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());

            if (departementId != null) {
                return "redirect:/superadmin/departements/" + departementId;
            }
            return "redirect:/superadmin/services";
        }
    }


    // ==================== SERVICES PAR DÉPARTEMENT ====================

    /**
     * Liste des services d'un département
     */
    @GetMapping("/departement/{departementId}")
    public String listServicesByDepartement(@PathVariable Long departementId,
                                            Model model,
                                            RedirectAttributes redirectAttributes) {
        try {
            Departement departement = departementService.findById(departementId);
            List<ServiceMunicipal> services = serviceMunicipalService.findServicesByDepartement(departementId);

            model.addAttribute("departement", departement);
            model.addAttribute("services", serviceMunicipalMapper.toDtoList(services));
            return "superadmin/services/list-by-departement";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Département introuvable");
            return "redirect:/superadmin/departements";
        }
    }
}