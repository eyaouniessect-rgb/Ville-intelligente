package com.ville.gestionincidents.service.incident;

import com.ville.gestionincidents.dto.incident.IncidentCreateDto;

import com.ville.gestionincidents.entity.*;
import com.ville.gestionincidents.enumeration.PrioriteIncident;

import com.ville.gestionincidents.dto.incident.IncidentDetailsDto;
import com.ville.gestionincidents.dto.incident.IncidentListDto;
import com.ville.gestionincidents.entity.Incident;
import com.ville.gestionincidents.entity.Photo;
import com.ville.gestionincidents.entity.Utilisateur;

import com.ville.gestionincidents.enumeration.StatutIncident;
import com.ville.gestionincidents.enumeration.TypeNotification;
import com.ville.gestionincidents.mapper.IncidentMapper;

import com.ville.gestionincidents.repository.*;

import com.ville.gestionincidents.repository.IncidentRepository;
import com.ville.gestionincidents.repository.PhotoRepository;
import com.ville.gestionincidents.security.CurrentUserService;
import com.ville.gestionincidents.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.ville.gestionincidents.repository.UtilisateurRepository;

import java.time.LocalDate;
import java.util.List;

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

    /* ===================== CRÉATION ===================== */

    @Override
    public void creerIncident(IncidentCreateDto dto) {

        Incident incident = incidentMapper.toEntity(dto);

        Utilisateur citoyen = currentUserService.getCurrentUser();
        incident.setCitoyen(citoyen);
        incident.setStatut(StatutIncident.SIGNALE);

        incident = incidentRepository.save(incident);

        notificationService.creerNotification(
                citoyen.getEmail(),
                TypeNotification.CREATION_INCIDENT,
                "Votre incident a été créé avec succès",
                incident
        );

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
                .countByService_DepartementAndDateDeclarationBetween(
                        d,
                        LocalDate.now().minusYears(50).atStartOfDay(),
                        LocalDate.now().atTime(23, 59, 59)
                );
    }

    @Override
    public long countByDepartementAndStatut(Departement d, StatutIncident s) {
        return incidentRepository
                .countByService_DepartementAndDateDeclarationBetweenAndStatut(
                        d,
                        LocalDate.now().minusYears(50).atStartOfDay(),
                        LocalDate.now().atTime(23, 59, 59),
                        s
                );
    }

    @Override
    public long countByDepartementAndStatutsEnCours(Departement d) {
        return incidentRepository
                .countByService_DepartementAndDateDeclarationBetweenAndStatutIn(
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
                dd.atStartOfDay(),
                df.atTime(23, 59, 59),
                pageable
        );
    }

    /* ===================== ASSIGNATION (FIX FINAL) ===================== */

    @Override
    public void assignerIncident(Long incidentId, Long serviceId, Long agentId, String commentaire, PrioriteIncident priorite   ) {

        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new RuntimeException("Incident introuvable"));

        ServiceMunicipal service = serviceMunicipalRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service introuvable"));

        Utilisateur agent = utilisateurRepository.findById(agentId)
                .orElseThrow(() -> new RuntimeException("Agent introuvable"));

        incident.setService(service);
        incident.setAgent(agent);
        incident.setStatut(StatutIncident.PRIS_EN_CHARGE);
        incident.setPriorite(priorite);

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

}
