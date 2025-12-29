package com.ville.gestionincidents.service.incident;

import com.ville.gestionincidents.entity.Departement;
import com.ville.gestionincidents.entity.Incident;
import com.ville.gestionincidents.entity.Quartier;
import com.ville.gestionincidents.entity.ServiceMunicipal;
import com.ville.gestionincidents.entity.Utilisateur;
import com.ville.gestionincidents.enumeration.Role;
import com.ville.gestionincidents.enumeration.StatutIncident;
import com.ville.gestionincidents.enumeration.TypeNotification;
import com.ville.gestionincidents.repository.IncidentRepository;
import com.ville.gestionincidents.repository.UtilisateurRepository;
import com.ville.gestionincidents.security.CurrentUserService;
import com.ville.gestionincidents.service.notification.NotificationService;
import com.ville.gestionincidents.service.notification.PreferenceNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour la fonctionnalité de clôture d'incident par le citoyen.
 */
@ExtendWith(MockitoExtension.class)
class IncidentServiceClotureTest {

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private PreferenceNotificationService preferenceNotificationService;

    @InjectMocks
    private IncidentServiceImpl incidentService;

    private Utilisateur citoyen;
    private Utilisateur autreCitoyen;
    private Utilisateur agent;
    private Utilisateur admin;
    private Departement departement;
    private Quartier quartier;
    private ServiceMunicipal serviceMunicipal;
    private Incident incident;

    @BeforeEach
    void setUp() {
        // ===== DÉPARTEMENT =====
        departement = new Departement();
        departement.setId(1L);
        departement.setNom("Travaux Publics");

        // ===== QUARTIER =====
        quartier = new Quartier();
        quartier.setId(1L);
        quartier.setNom("Centre-Ville");

        // ===== SERVICE MUNICIPAL =====
        serviceMunicipal = new ServiceMunicipal();
        serviceMunicipal.setId(1L);
        serviceMunicipal.setNom("Service Eau");
        serviceMunicipal.setDepartement(departement);

        // ===== CITOYEN PROPRIÉTAIRE =====
        citoyen = new Utilisateur();
        citoyen.setId(1L);
        citoyen.setNom("Dupont");
        citoyen.setPrenom("Jean");
        citoyen.setEmail("citoyen@test.com");
        citoyen.setRole(Role.CITOYEN);

        // ===== AUTRE CITOYEN =====
        autreCitoyen = new Utilisateur();
        autreCitoyen.setId(2L);
        autreCitoyen.setNom("Martin");
        autreCitoyen.setPrenom("Marie");
        autreCitoyen.setEmail("autre@test.com");
        autreCitoyen.setRole(Role.CITOYEN);

        // ===== AGENT =====
        agent = new Utilisateur();
        agent.setId(3L);
        agent.setNom("Agent");
        agent.setPrenom("Test");
        agent.setEmail("agent@test.com");
        agent.setRole(Role.AGENT);

        // ===== ADMIN =====
        admin = new Utilisateur();
        admin.setId(4L);
        admin.setNom("Admin");
        admin.setPrenom("Test");
        admin.setEmail("admin@test.com");
        admin.setRole(Role.ADMIN);
        admin.setDepartement(departement);

        // ===== INCIDENT DE BASE =====
        incident = new Incident();
        incident.setId(1L);
        incident.setDescription("Fuite d'eau importante");
        incident.setLatitude(36.8065);
        incident.setLongitude(10.1815);
        incident.setCitoyen(citoyen);
        incident.setDepartement(departement);
        incident.setQuartier(quartier);
        incident.setService(serviceMunicipal);
        incident.setAgent(agent);
        incident.setStatut(StatutIncident.RESOLU);
        incident.setDateDeclaration(LocalDateTime.now().minusDays(5));
        incident.setDateDerniereMiseAJour(LocalDateTime.now().minusDays(1));
    }

    // ======================================================
    // TC1 — Clôture réussie de l'incident par le citoyen concerné
    // ======================================================
    @Test
    @DisplayName("TC1 - Clôture réussie de l'incident par le citoyen concerné")
    void tc1_cloture_reussie_citoyen_proprietaire() {
        // Arrange - Préconditions : Incident existant, citoyen propriétaire, statut = RESOLU
        when(currentUserService.getCurrentUser()).thenReturn(citoyen);
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(incidentRepository.save(any(Incident.class))).thenReturn(incident);
        when(utilisateurRepository.findByRoleAndDepartement_Id(Role.ADMIN, 1L))
                .thenReturn(Optional.of(admin));

        // Act
        incidentService.cloturerIncidentParCitoyen(1L);

        // Assert - Résultat attendu : Statut passe à CLOTURE, date de mise à jour renseignée, test SUCCESS
        verify(incidentRepository, times(1)).findById(1L);
        verify(incidentRepository, times(1)).save(any(Incident.class));

        // Vérifier que le statut est changé en CLOTURE
        verify(incidentRepository).save(argThat(inc ->
                inc.getStatut() == StatutIncident.CLOTURE &&
                        inc.getDateDerniereMiseAJour() != null
        ));

        // Vérifier les notifications envoyées (agent + admin)
        verify(notificationService, times(2)).creerNotification(
                anyString(),
                eq(TypeNotification.CHANGEMENT_STATUT),
                anyString(),
                any(Incident.class)
        );
    }

    // ======================================================
    // TC2 — Tentative de clôture d'un incident inexistant
    // ======================================================
    @Test
    @DisplayName("TC2 - Tentative de clôture d'un incident inexistant")
    void tc2_incident_inexistant() {
        // Arrange - Préconditions : Aucun incident avec l'identifiant fourni
        when(incidentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert - Résultat attendu : Exception 'Incident introuvable', test SUCCESS
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> incidentService.cloturerIncidentParCitoyen(999L)
        );

        assertEquals("Incident introuvable", exception.getMessage());
        verify(incidentRepository, times(1)).findById(999L);
        verify(incidentRepository, never()).save(any(Incident.class));
        verify(notificationService, never()).creerNotification(
                anyString(), any(), anyString(), any()
        );
    }

    // ======================================================
    // TC3 — Tentative de clôture par un citoyen non autorisé
    // ======================================================
    @Test
    @DisplayName("TC3 - Tentative de clôture par un citoyen non autorisé")
    void tc3_citoyen_non_autorise() {
        // Arrange - Préconditions : Incident existant mais citoyen courant ≠ déclarant
        when(currentUserService.getCurrentUser()).thenReturn(autreCitoyen);
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));

        // Act & Assert - Résultat attendu : Exception 'Vous n'êtes pas autorisé à clôturer cet incident', test SUCCESS
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> incidentService.cloturerIncidentParCitoyen(1L)
        );

        assertEquals(
                "Vous n'êtes pas autorisé à clôturer cet incident",
                exception.getMessage()
        );

        verify(incidentRepository, times(1)).findById(1L);
        verify(incidentRepository, never()).save(any(Incident.class));
        verify(notificationService, never()).creerNotification(
                anyString(), any(), anyString(), any()
        );
    }

    // ======================================================
    // TC4 — Tentative de clôture d'un incident non résolu
    // ======================================================
    @Test
    @DisplayName("TC4 - Tentative de clôture d'un incident non résolu")
    void tc4_incident_non_resolu() {
        // Arrange - Préconditions : Incident existant, citoyen propriétaire, statut ≠ RESOLU
        incident.setStatut(StatutIncident.SIGNALE); // ou PRIS_EN_CHARGE, EN_RESOLUTION
        when(currentUserService.getCurrentUser()).thenReturn(citoyen);
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));

        // Act & Assert - Résultat attendu : Exception 'Seuls les incidents résolus peuvent être clôturés', test SUCCESS
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> incidentService.cloturerIncidentParCitoyen(1L)
        );

        assertEquals(
                "Seuls les incidents résolus peuvent être clôturés",
                exception.getMessage()
        );

        verify(incidentRepository, times(1)).findById(1L);
        verify(incidentRepository, never()).save(any(Incident.class));
    }
}