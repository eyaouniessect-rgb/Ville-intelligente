package com.ville.gestionincidents.service.serviceMunicipal;

import com.ville.gestionincidents.entity.Departement;
import com.ville.gestionincidents.entity.Incident;
import com.ville.gestionincidents.entity.ServiceMunicipal;
import com.ville.gestionincidents.repository.DepartementRepository;
import com.ville.gestionincidents.repository.ServiceMunicipalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceMunicipalServiceTest {

    @Mock
    private ServiceMunicipalRepository serviceMunicipalRepository;

    @Mock
    private DepartementRepository departementRepository;

    @InjectMocks
    private ServiceMunicipalServiceImpl service;

    private Departement departement;
    private ServiceMunicipal serviceMunicipal;

    @BeforeEach
    void setup() {
        departement = new Departement();
        departement.setId(1L);
        departement.setNom("Voirie");

        serviceMunicipal = new ServiceMunicipal();
        serviceMunicipal.setId(10L);
        serviceMunicipal.setNom("Nettoyage");
        serviceMunicipal.setDescription("Service de nettoyage");
        serviceMunicipal.setDepartement(departement);
    }

    // ===================== ADD SERVICE =====================

    @Test
    @DisplayName("Ajouter un service à un département avec succès")
    void add_service_success() {

        when(departementRepository.findById(1L))
                .thenReturn(Optional.of(departement));
        when(serviceMunicipalRepository
                .findByNomAndDepartement("Nettoyage", departement))
                .thenReturn(Optional.empty());
        when(serviceMunicipalRepository.save(any()))
                .thenAnswer(inv -> inv.getArgument(0));

        ServiceMunicipal result =
                service.addServiceToDepartement(1L, serviceMunicipal);

        assertEquals("Nettoyage", result.getNom());
        assertEquals(departement, result.getDepartement());
    }

    @Test
    @DisplayName("Échec ajout - département introuvable")
    void add_service_departement_introuvable() {

        when(departementRepository.findById(1L))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.addServiceToDepartement(1L, serviceMunicipal));

        assertEquals("Département introuvable", ex.getMessage());
    }

    @Test
    @DisplayName("Échec ajout - service déjà existant dans le département")
    void add_service_duplicate_name() {

        when(departementRepository.findById(1L))
                .thenReturn(Optional.of(departement));
        when(serviceMunicipalRepository
                .findByNomAndDepartement("Nettoyage", departement))
                .thenReturn(Optional.of(new ServiceMunicipal()));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.addServiceToDepartement(1L, serviceMunicipal));

        assertEquals(
                "Un service avec ce nom existe déjà dans ce département",
                ex.getMessage()
        );
    }

    // ===================== UPDATE SERVICE =====================

    @Test
    @DisplayName("Modifier un service avec succès")
    void update_service_success() {

        ServiceMunicipal updated = new ServiceMunicipal();
        updated.setNom("Propreté");
        updated.setDescription("Nouvelle description");

        when(serviceMunicipalRepository.findById(10L))
                .thenReturn(Optional.of(serviceMunicipal));
        when(serviceMunicipalRepository
                .findByNomAndDepartement("Propreté", departement))
                .thenReturn(Optional.empty());
        when(serviceMunicipalRepository.save(any()))
                .thenAnswer(inv -> inv.getArgument(0));

        ServiceMunicipal result =
                service.updateService(10L, updated);

        assertEquals("Propreté", result.getNom());
        assertEquals("Nouvelle description", result.getDescription());
    }

    // ===================== DELETE SERVICE =====================

    @Test
    @DisplayName("Échec suppression - service contient des incidents")
    void delete_service_with_incidents() {

        Incident incident = new Incident();
        serviceMunicipal.setIncidents(
                Collections.singletonList(incident)
        );

        when(serviceMunicipalRepository.findById(10L))
                .thenReturn(Optional.of(serviceMunicipal));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.deleteService(10L));

        assertTrue(ex.getMessage()
                .contains("Impossible de supprimer ce service"));
    }
}
