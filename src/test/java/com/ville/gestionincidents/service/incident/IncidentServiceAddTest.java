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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncidentServiceAddTest {

    @Mock private IncidentRepository incidentRepository;
    @Mock private PhotoRepository photoRepository;
    @Mock private IncidentMapper incidentMapper;
    @Mock private PhotoStorageService photoStorageService;
    @Mock private CurrentUserService currentUserService;
    @Mock private NotificationService notificationService;
    @Mock private DepartementRepository departementRepository;
    @Mock private QuartierRepository quartierRepository;
    @Mock private PreferenceNotificationService preferenceNotificationService;

    @InjectMocks
    private IncidentServiceImpl incidentService;

    private Utilisateur citoyen;
    private Departement departement;
    private Quartier quartier;
    private IncidentCreateDto dto;

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

        dto = new IncidentCreateDto();
        dto.setDescription("Incident test");
        dto.setLatitude(36.8);
        dto.setLongitude(10.1);
        dto.setDepartementId(1L);
        dto.setQuartierId(1L);
    }

    // ===================== CAS 1 =====================
    @Test
    @DisplayName("Créer un incident - succès")
    void creerIncident_success() {

        when(currentUserService.getCurrentUser()).thenReturn(citoyen);
        when(departementRepository.findById(1L)).thenReturn(Optional.of(departement));
        when(quartierRepository.findById(1L)).thenReturn(Optional.of(quartier));
        when(incidentRepository.save(any(Incident.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        incidentService.creerIncident(dto);

        verify(incidentRepository, times(1)).save(any(Incident.class));
        verify(notificationService, times(1))
                .creerNotification(eq("citoyen@test.com"), any(), anyString(), any());
    }

    // ===================== CAS 2 =====================
    @Test
    @DisplayName("Créer un incident - département introuvable")
    void creerIncident_departementIntrouvable() {

        when(currentUserService.getCurrentUser()).thenReturn(citoyen);
        when(departementRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> incidentService.creerIncident(dto));

        verify(incidentRepository, never()).save(any());
    }

    // ===================== CAS 3 =====================
    @Test
    @DisplayName("Créer un incident - quartier introuvable")
    void creerIncident_quartierIntrouvable() {

        when(currentUserService.getCurrentUser()).thenReturn(citoyen);
        when(departementRepository.findById(1L)).thenReturn(Optional.of(departement));
        when(quartierRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> incidentService.creerIncident(dto));

        verify(incidentRepository, never()).save(any());
    }

    // ===================== CAS 4 =====================
    @Test
    @DisplayName("Créer un incident sans photos")
    void creerIncident_sansPhotos() {

        dto.setPhotos(null);

        when(currentUserService.getCurrentUser()).thenReturn(citoyen);
        when(departementRepository.findById(1L)).thenReturn(Optional.of(departement));
        when(quartierRepository.findById(1L)).thenReturn(Optional.of(quartier));
        when(incidentRepository.save(any(Incident.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        incidentService.creerIncident(dto);

        verify(incidentRepository).save(any());
        verify(photoRepository, never()).save(any());
    }
    // ======================CAS 5 =====================

    @Test
    @DisplayName("Créer un incident - utilisateur non connecté")
    void creerIncident_userNonConnecte() {

        // Arrange
        when(currentUserService.getCurrentUser()).thenReturn(null);

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> incidentService.creerIncident(dto));

        // Assert
        verify(incidentRepository, never()).save(any());
        verify(notificationService, never()).creerNotification(any(), any(), any(), any());
    }
}
