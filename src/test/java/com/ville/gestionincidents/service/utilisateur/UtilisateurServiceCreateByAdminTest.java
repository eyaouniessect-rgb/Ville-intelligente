package com.ville.gestionincidents.service.utilisateur;

import com.ville.gestionincidents.dto.utilisateur.superAdmin.CreateUtilisateurByAdminDto;
import com.ville.gestionincidents.entity.Departement;
import com.ville.gestionincidents.entity.ServiceMunicipal;
import com.ville.gestionincidents.entity.Utilisateur;
import com.ville.gestionincidents.enumeration.Role;
import com.ville.gestionincidents.mapper.UtilisateurMapper;
import com.ville.gestionincidents.repository.DepartementRepository;
import com.ville.gestionincidents.repository.ServiceMunicipalRepository;
import com.ville.gestionincidents.repository.UtilisateurRepository;
import com.ville.gestionincidents.service.email.EmailService;
import com.ville.gestionincidents.service.password.PasswordGeneratorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UtilisateurServiceCreateByAdminTest {

    @Mock
    private DepartementRepository departementRepository;
    @Mock private UtilisateurRepository utilisateurRepository;
    @Mock private ServiceMunicipalRepository serviceMunicipalRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EmailService emailService;
    @Mock private PasswordGeneratorService passwordGeneratorService;
    @Mock private UtilisateurMapper utilisateurMapper;

    @InjectMocks
    private UtilisateurServiceImpl service;

    private CreateUtilisateurByAdminDto dto;
    private Departement departement;

    @BeforeEach
    void setup() {
        dto = new CreateUtilisateurByAdminDto();
        dto.setNom("Admin");
        dto.setPrenom("Test");
        dto.setEmail("admin@test.com");
        dto.setDepartementId(1L);

        departement = new Departement();
        departement.setId(1L);
    }

    // ===================== SUCCESS =====================
    @Test
    @DisplayName("Créer un ADMIN avec succès")
    void create_admin_success() {

        when(passwordGeneratorService.generatePassword()).thenReturn("Temp@12345678");
        when(utilisateurRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(departementRepository.findById(1L)).thenReturn(Optional.of(departement));
        when(utilisateurRepository.save(any(Utilisateur.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Utilisateur result = service.createUserByAdmin(dto, Role.ADMIN);

        assertEquals(Role.ADMIN, result.getRole());
        assertTrue(result.isEmailVerifie());
        verify(emailService).sendWelcomeEmail(
                eq("admin@test.com"),
                eq("Admin"),
                eq(Role.ADMIN),
                anyString()
        );
    }

    // ===================== EMAIL EXISTANT =====================
    @Test
    @DisplayName("Échec création - email déjà utilisé")
    void create_user_email_existant() {

        when(utilisateurRepository.findByEmail(dto.getEmail()))
                .thenReturn(Optional.of(new Utilisateur()));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.createUserByAdmin(dto, Role.ADMIN));

        assertEquals("Cet email est déjà utilisé", ex.getMessage());
        verify(utilisateurRepository, never()).save(any());
    }

    // ===================== DÉPARTEMENT INTROUVABLE =====================
    @Test
    @DisplayName("Échec création - département introuvable")
    void create_user_departement_introuvable() {

        when(passwordGeneratorService.generatePassword()).thenReturn("Temp@12345678");
        when(utilisateurRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(departementRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.createUserByAdmin(dto, Role.ADMIN));

        assertEquals("Département introuvable", ex.getMessage());
    }

    // ===================== AGENT AVEC SERVICE =====================
    @Test
    @DisplayName("Créer un AGENT avec service municipal")
    void create_agent_with_service() {

        ServiceMunicipal serviceMunicipal = new ServiceMunicipal();
        serviceMunicipal.setId(5L);
        dto.setServiceId(5L);

        when(passwordGeneratorService.generatePassword()).thenReturn("Temp@12345678");
        when(utilisateurRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(departementRepository.findById(1L)).thenReturn(Optional.of(departement));
        when(serviceMunicipalRepository.findById(5L)).thenReturn(Optional.of(serviceMunicipal));
        when(utilisateurRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Utilisateur result = service.createUserByAdmin(dto, Role.AGENT);

        assertEquals(Role.AGENT, result.getRole());
        assertNotNull(result.getServiceMunicipal());
    }
}
