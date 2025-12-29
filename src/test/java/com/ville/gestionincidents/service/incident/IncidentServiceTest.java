package com.ville.gestionincidents.service.incident;

import com.ville.gestionincidents.dto.incident.IncidentCreateDto;
import com.ville.gestionincidents.entity.*;
import com.ville.gestionincidents.enumeration.Role;
import com.ville.gestionincidents.enumeration.StatutIncident;
import com.ville.gestionincidents.mapper.IncidentMapper;
import com.ville.gestionincidents.repository.*;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncidentServiceImplTest {

    @Mock private IncidentRepository incidentRepository;
    @Mock private PhotoRepository photoRepository;
    @Mock private IncidentMapper incidentMapper;
    @Mock private PhotoStorageService photoStorageService;
    @Mock private CurrentUserService currentUserService;
    @Mock private NotificationService notificationService;
    @Mock private ServiceMunicipalRepository serviceMunicipalRepository;
    @Mock private UtilisateurRepository utilisateurRepository;
    @Mock private DepartementRepository departementRepository;
    @Mock private QuartierRepository quartierRepository;
    @Mock private PreferenceNotificationService preferenceNotificationService;

    @InjectMocks
    private IncidentServiceImpl incidentService;

    private Utilisateur citoyen;
    private Departement departement;
    private Quartier quartier;
    private Incident incident;
    private IncidentCreateDto incidentCreateDto;

    @BeforeEach
    void setUp() {

        citoyen = new Utilisateur();
        citoyen.setId(1L);
        citoyen.setEmail("citoyen@test.com");
        citoyen.setRole(Role.CITOYEN);

        departement = new Departement();
        departement.setId(1L);

        quartier = new Quartier();
        quartier.setId(1L);

        incident = new Incident();
        incident.setId(1L);
        incident.setCitoyen(citoyen);
        incident.setDepartement(departement);
        incident.setQuartier(quartier);
        incident.setStatut(StatutIncident.SIGNALE);
        incident.setDateDeclaration(LocalDateTime.now());

        incidentCreateDto = new IncidentCreateDto();
        incidentCreateDto.setDescription("Incident test");
        incidentCreateDto.setLatitude(36.8);
        incidentCreateDto.setLongitude(10.1);
        incidentCreateDto.setDepartementId(1L);
        incidentCreateDto.setQuartierId(1L);
    }

    // ========================= TEST 1 =========================
    @Test
    @DisplayName("Créer un incident avec succès")
    void creerIncident_success() {

        when(currentUserService.getCurrentUser()).thenReturn(citoyen);
        when(departementRepository.findById(1L)).thenReturn(Optional.of(departement));
        when(quartierRepository.findById(1L)).thenReturn(Optional.of(quartier));
        when(incidentRepository.save(any(Incident.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        incidentService.creerIncident(incidentCreateDto);

        verify(incidentRepository, times(1)).save(any(Incident.class));
        verify(notificationService, times(1))
                .creerNotification(
                        eq("citoyen@test.com"),
                        any(),
                        anyString(),
                        any(Incident.class)
                );
    }

    // ========================= TEST 2 =========================
    @Test
    @DisplayName("Récupérer les incidents du citoyen courant")
    void getIncidentsForCurrentUser_success() {

        when(currentUserService.getCurrentUser()).thenReturn(citoyen);
        when(incidentRepository.findByCitoyen(citoyen))
                .thenReturn(List.of(incident));
        when(incidentMapper.toListDtos(any()))
                .thenReturn(List.of());

        var result = incidentService.getIncidentsForCurrentUser();

        assertNotNull(result);
        verify(incidentRepository).findByCitoyen(citoyen);
        verify(incidentMapper).toListDtos(any());
    }

    // ========================= TEST 3 =========================
    @Test
    @DisplayName("Compter les incidents SIGNALE pour le citoyen courant")
    void countSignaleForCurrentUser_success() {

        when(currentUserService.getCurrentUser()).thenReturn(citoyen);
        when(incidentRepository.countByCitoyenEmailAndStatut(
                "citoyen@test.com",
                StatutIncident.SIGNALE
        )).thenReturn(5);

        int count = incidentService.countSignaleForCurrentUser();

        assertEquals(5, count);
        verify(incidentRepository)
                .countByCitoyenEmailAndStatut("citoyen@test.com", StatutIncident.SIGNALE);
    }
}
