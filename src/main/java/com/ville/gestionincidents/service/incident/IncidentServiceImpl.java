package com.ville.gestionincidents.service.incident;

import com.ville.gestionincidents.dto.incident.IncidentCreateDto;

import com.ville.gestionincidents.entity.*;
import com.ville.gestionincidents.enumeration.*;

import com.ville.gestionincidents.dto.incident.IncidentDetailsDto;
import com.ville.gestionincidents.dto.incident.IncidentListDto;
import com.ville.gestionincidents.entity.Incident;
import com.ville.gestionincidents.entity.Photo;
import com.ville.gestionincidents.entity.Utilisateur;

import com.ville.gestionincidents.mapper.IncidentMapper;

import com.ville.gestionincidents.repository.*;

import com.ville.gestionincidents.repository.IncidentRepository;
import com.ville.gestionincidents.repository.PhotoRepository;
import com.ville.gestionincidents.security.CurrentUserService;
import com.ville.gestionincidents.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.ville.gestionincidents.repository.UtilisateurRepository;
import com.ville.gestionincidents.repository.DepartementRepository;
import com.ville.gestionincidents.repository.QuartierRepository;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.ville.gestionincidents.enumeration.TypeNotification;
import com.ville.gestionincidents.service.notification.PreferenceNotificationService;

@Service
@RequiredArgsConstructor
public class IncidentServiceImpl implements IncidentService {

    private final IncidentRepository incidentRepository;
    private final PhotoRepository photoRepository;
    private final IncidentMapper incidentMapper;
    private final PhotoStorageService photoStorageService;
    private final CurrentUserService currentUserService;
    private final NotificationService notificationService;
    private final ServiceMunicipalRepository serviceMunicipalRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final DepartementRepository departementRepository;
    private  final QuartierRepository quartierRepository;
    private final PreferenceNotificationService preferenceNotificationService;
    /* ===================== CRÉATION ===================== */

    @Override
    public void creerIncident(IncidentCreateDto dto) {

        // 🔹 Utilisateur connecté
        Utilisateur citoyen = currentUserService.getCurrentUser();

        // 🔹 Récupération du département (catégorie)
        Departement departement = departementRepository.findById(dto.getDepartementId())
                .orElseThrow(() -> new RuntimeException("Département introuvable"));

        // 🔹 Récupération du quartier
        Quartier quartier = quartierRepository.findById(dto.getQuartierId())
                .orElseThrow(() -> new RuntimeException("Quartier introuvable"));

        // 🔹 Création de l'incident
        Incident incident = new Incident();
        incident.setDescription(dto.getDescription());
        incident.setLatitude(dto.getLatitude());
        incident.setLongitude(dto.getLongitude());
        incident.setDepartement(departement);
        incident.setQuartier(quartier);
        incident.setCitoyen(citoyen);
        incident.setStatut(StatutIncident.SIGNALE);
        incident.setDateDeclaration(LocalDateTime.now());

        // 🔹 Sauvegarde incident
        incident = incidentRepository.save(incident);

        // 🔔 Notification
        notificationService.creerNotification(
                citoyen.getEmail(),
                TypeNotification.CREATION_INCIDENT,
                "Votre incident a été créé avec succès",
                incident
        );

        // 📸 Sauvegarde des photos
        if (dto.getPhotos() != null) {
            boolean principale = true;

            for (MultipartFile f : dto.getPhotos()) {
                if (f == null || f.isEmpty()) continue;

                String nom = photoStorageService.save(f);

                Photo photo = new Photo();
                photo.setNomFichier(nom);
                photo.setTypeContenu(f.getContentType());
                photo.setCheminStockage("uploads/" + nom);
                photo.setPrincipale(principale);
                photo.setIncident(incident);

                photoRepository.save(photo);
                principale = false;
            }
        }
    }


    /* ===================== CITOYEN ===================== */

    @Override

    public List<IncidentListDto> getIncidentsForCurrentUser() {
        Utilisateur user = currentUserService.getCurrentUser();
        List<Incident> incidents = incidentRepository.findByCitoyen(user);
        return incidentMapper.toListDtos(incidents);

    }

    @Override

    public List<IncidentListDto> getIncidentsByStatutForCurrentUser(StatutIncident statut) {
        Utilisateur user = currentUserService.getCurrentUser();
        List<Incident> incidents = incidentRepository.findByCitoyenIdAndStatut(user.getId(), statut);
        return incidentMapper.toListDtos(incidents);
    }


    @Override
    public IncidentDetailsDto getIncidentDetailsForCurrentUser(Long id) {
        Incident inc = incidentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Incident introuvable"));

        if (!inc.getCitoyen().getId().equals(currentUserService.getCurrentUserId())) {
            throw new RuntimeException("Accès non autorisé à cet incident !");
        }

        return incidentMapper.toDetailsDto(inc);

    }

   


    @Override
    public Incident findByIdAndCheckOwner(Long id, String email) {
        Incident inc = incidentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Incident introuvable"));

        if (!inc.getCitoyen().getEmail().equals(email)) {
            throw new RuntimeException("Accès non autorisé");
        }
        return inc;
    }

    /* ===================== ADMIN ===================== */

    @Override
    public long countByDepartement(Departement d) {
        return incidentRepository
                .countByDepartementAndDateDeclarationBetween(
                        d,
                        LocalDate.now().minusYears(50).atStartOfDay(),
                        LocalDate.now().atTime(23, 59, 59)
                );
    }

    @Override
    public long countByDepartementAndStatut(Departement departement, StatutIncident statut) {
        return incidentRepository.countByDepartementAndStatut(departement, statut);
    }

    @Override
    public long countByDepartementAndStatutsEnCours(Departement d) {
        return incidentRepository
                .countByDepartementAndDateDeclarationBetweenAndStatutIn(
                        d,
                        LocalDate.now().minusYears(50).atStartOfDay(),
                        LocalDate.now().atTime(23, 59, 59),
                        List.of(
                                StatutIncident.PRIS_EN_CHARGE,
                                StatutIncident.EN_RESOLUTION
                        )
                );
    }
    @Override
    public long countNonAssignesByDepartement(Departement departement) {
        return incidentRepository.countNonAssignesByDepartement(departement);
    }

    @Override
    public long countByDepartementAndServiceIsNull(Departement d) {
        return incidentRepository.findByDateDeclarationBetween(
                        LocalDate.now().minusYears(50).atStartOfDay(),
                        LocalDate.now().atTime(23, 59, 59)
                )
                .stream()
                .filter(i -> i.getService() == null ||
                        i.getService().getDepartement().equals(d))
                .count();
    }

    @Override
    public Page<Incident> findByDepartementWithFilters(
            Departement d,
            Long serviceId,
            String statut,
            LocalDate dd,
            LocalDate df,
            int page,
            int size
    ) {
        // 🔧 CORRECTION: Gérer les dates null
        LocalDateTime dateDebut = (dd != null)
                ? dd.atStartOfDay()
                : LocalDate.now().minusYears(10).atStartOfDay();  // Plage très large

        LocalDateTime dateFin = (df != null)
                ? df.atTime(23, 59, 59)
                : LocalDate.now().atTime(23, 59, 59);

        StatutIncident s = (statut == null || statut.isBlank())
                ? null
                : StatutIncident.valueOf(statut);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("dateDeclaration").descending()
        );

        return incidentRepository.findByFilters(
                d,
                serviceId,
                s,
                dateDebut,   // ✅ LocalDateTime
                dateFin,     // ✅ LocalDateTime
                pageable
        );
    }

    /* ===================== ASSIGNATION (FIX FINAL) ===================== */

    @Override
    public void assignerIncident(Long incidentId, Long serviceId, Long agentId, PrioriteIncident priorite   ) {

        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new RuntimeException("Incident introuvable"));

        ServiceMunicipal service = serviceMunicipalRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service introuvable"));

        Utilisateur agent = utilisateurRepository.findById(agentId)
                .orElseThrow(() -> new RuntimeException("Agent introuvable"));
        Utilisateur citoyen = utilisateurRepository.findById(incident.getCitoyen().getId())
                .orElseThrow(() -> new RuntimeException("Citoyen introuvable"));
        incident.setService(service);
        incident.setAgent(agent);
        incident.setStatut(StatutIncident.PRIS_EN_CHARGE);
        incident.setPriorite(priorite);

        preferenceNotificationService.getOrCreate(agent.getId());

        notificationService.creerNotification(
                agent.getEmail(),
                TypeNotification.ASSIGNATION,
                "Vous avez été assigné à un nouvel incident : " + incident.getId(),
                incident
        );
        notificationService.creerNotification(
                citoyen.getEmail(),
                TypeNotification.CHANGEMENT_STATUT,
                "Vous incident est pris en charge  : " + incident.getId(),
                incident
        );
        incidentRepository.save(incident);
    }

    public int countForCurrentUser() {
        String email = currentUserService.getCurrentUser().getEmail();
        return incidentRepository.countByCitoyenEmail(email);
    }

    @Override
    public int countSignaleForCurrentUser() {
        String email = currentUserService.getCurrentUser().getEmail();
        return incidentRepository.countByCitoyenEmailAndStatut(email, StatutIncident.SIGNALE);
    }

    @Override
    public int countPrisEnChargeForCurrentUser() {
        String email = currentUserService.getCurrentUser().getEmail();
        return incidentRepository.countByCitoyenEmailAndStatut(email, StatutIncident.PRIS_EN_CHARGE);
    }

    @Override
    public int countEnResolutionForCurrentUser() {
        String email = currentUserService.getCurrentUser().getEmail();
        return incidentRepository.countByCitoyenEmailAndStatut(email, StatutIncident.EN_RESOLUTION);
    }

    @Override
    public int countResoluForCurrentUser() {
        String email = currentUserService.getCurrentUser().getEmail();
        return incidentRepository.countByCitoyenEmailAndStatut(email, StatutIncident.RESOLU);
    }

    @Override
    public int countClotureForCurrentUser() {
        String email = currentUserService.getCurrentUser().getEmail();
        return incidentRepository.countByCitoyenEmailAndStatut(email, StatutIncident.CLOTURE);
    }
// Dans IncidentServiceImpl.java

    @Override
    public long countByAgent(Utilisateur agent) {
        return incidentRepository.countByAgent(agent);
    }

    @Override
    public long countByAgentAndStatut(Utilisateur agent, StatutIncident statut) {
        return incidentRepository.countByAgentAndStatut(agent, statut);
    }

    @Override
    public List<Incident> findByAgent(Utilisateur agent) {
        return incidentRepository.findByAgentOrderByDateDeclarationDesc(agent);
    }

    @Override
    public List<Incident> findByAgentAndStatut(Utilisateur agent, StatutIncident statut) {
        return incidentRepository.findByAgentAndStatutOrderByDateDeclarationDesc(agent, statut);
    }

    @Override
    @Transactional
    public void changerStatut(Long incidentId,
                              StatutIncident nouveauStatut) {

        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new RuntimeException("Incident introuvable"));

        Utilisateur agent = currentUserService.getCurrentUser();

        // 🔐 Sécurité
        if (incident.getAgent() == null ||
                !incident.getAgent().getId().equals(agent.getId())) {
            throw new RuntimeException("Accès interdit");
        }

        // 🔁 Mise à jour
        incident.setStatut(nouveauStatut);
        incident.setDateDerniereMiseAJour(LocalDateTime.now());
        incidentRepository.save(incident);

        /* 🔔 NOTIFICATION CITOYEN */
        notificationService.creerNotification(
                incident.getCitoyen().getEmail(),
                TypeNotification.CHANGEMENT_STATUT,
                "Votre incident #" + incident.getId()
                        + " est passé au statut : " + nouveauStatut,
                incident
        );

        /* 🔔 NOTIFICATION ADMIN (1 seul admin par département) */
        if (incident.getService() == null ||
                incident.getService().getDepartement() == null) {
            throw new RuntimeException("Incident sans service ou département");
        }

        Utilisateur admin =
                utilisateurRepository.findByRoleAndDepartement_Id(
                        Role.ADMIN,
                        incident.getService().getDepartement().getId()
                ).orElseThrow(() ->
                        new RuntimeException("Admin introuvable pour ce département"));
        System.out.println("SERVICE ID = " + incident.getService().getId());
        System.out.println("DEPARTEMENT ID = " + incident.getService().getDepartement().getId());
        preferenceNotificationService.getOrCreate(admin.getId());

        notificationService.creerNotification(
                admin.getEmail(),
                TypeNotification.CHANGEMENT_STATUT,
                "Incident #" + incident.getId()
                        + " changé en " + nouveauStatut
                        + " par l'agent " + agent.getNom(),
                incident
        );
    }


    @Override
    public Incident findById(Long id) {
        return incidentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Incident introuvable"));
    }

    @Override
    public List<Incident> getIncidentsNonAssignesParDepartement(Utilisateur admin) {

        Departement departement = admin.getDepartement();

        return incidentRepository
                .findIncidentsNonAssignesParDepartement(departement);
    }


}
