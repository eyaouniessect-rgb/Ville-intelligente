package com.ville.gestionincidents.controller.notification;

import com.ville.gestionincidents.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/citoyen")
public class NotificationController {

    private final NotificationService notificationService;

    // 📥 Afficher les notifications
    @GetMapping("/notifications")
    public String afficherNotifications(Model model, Principal principal) {

        String email = principal.getName();


        model.addAttribute(
                "notifications",
                notificationService.getNotificationsByEmail(email)
        );

        model.addAttribute(
                "nbNonLues",
                notificationService.countNotificationsNonLues(email)
        );

        return "citoyen/notifi_list";
    }

    // ✔ Marquer une notification comme lue
    @PostMapping("/notifications/{id}/lu")
    public String marquerCommeLue(@PathVariable Long id) {

        notificationService.marquerCommeLue(id);

        return "redirect:/citoyen/notifications";
    }
}
