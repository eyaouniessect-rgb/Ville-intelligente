package com.ville.gestionincidents.controller.incident;

import com.ville.gestionincidents.entity.Utilisateur;
import com.ville.gestionincidents.entity.Departement;
import com.ville.gestionincidents.entity.Quartier;
import com.ville.gestionincidents.enumeration.Role;
import com.ville.gestionincidents.repository.UtilisateurRepository;
import com.ville.gestionincidents.repository.DepartementRepository;
import com.ville.gestionincidents.repository.QuartierRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

/**
 * Test d'intégration pour le contrôleur d'incidents
 * Utilise une base de données MySQL de test et le vrai CurrentUserService
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class IncidentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private DepartementRepository departementRepository;

    @Autowired
    private QuartierRepository quartierRepository;

    private Utilisateur citoyen;
    private Utilisateur agent;
    private Departement departement;
    private Quartier quartier;

    @BeforeEach
    void setUp() {
        // Nettoyer les données de test précédentes
        // IMPORTANT: Respecter l'ordre à cause des contraintes de clés étrangères
        quartierRepository.deleteAll();
        departementRepository.deleteAll();
        utilisateurRepository.deleteAll();

        // Créer un citoyen de test
        // L'email DOIT correspondre au username dans @WithMockUser
        citoyen = new Utilisateur();
        citoyen.setNom("TestUser");
        citoyen.setPrenom("Jean");
        citoyen.setEmail("test@email.com");
        citoyen.setMotDePasse("$2a$10$testPassword");
        citoyen.setRole(Role.CITOYEN);
        citoyen = utilisateurRepository.saveAndFlush(citoyen);

        // Créer un agent de test
        agent = new Utilisateur();
        agent.setNom("Agent");
        agent.setPrenom("Marc");
        agent.setEmail("agent@email.com");
        agent.setMotDePasse("$2a$10$testPassword");
        agent.setRole(Role.AGENT);
        agent = utilisateurRepository.saveAndFlush(agent);

        // Créer un département de test
        departement = new Departement();
        departement.setNom("Test Département");
        departement = departementRepository.saveAndFlush(departement);

        // Créer un quartier de test
        quartier = new Quartier();
        quartier.setNom("Test Quartier");
        quartier.setCodePostal("1000");
        quartier.setDepartement(departement);
        quartier = quartierRepository.saveAndFlush(quartier);
    }

    @Test
    @DisplayName("Accès formulaire incident (authentifié)")
    @WithMockUser(username = "test@email.com", roles = "CITOYEN")
    void afficherFormulaireCreation_authentifie() throws Exception {
        mockMvc.perform(get("/citoyen/getFormIncident"))
                .andExpect(status().isOk())
                .andExpect(view().name("citoyen/incident_form"))
                .andExpect(model().attributeExists("incident"))
                .andExpect(model().attributeExists("departements"))
                .andExpect(model().attributeExists("quartiers"));
    }

    @Test
    @DisplayName("Accès formulaire incident non authentifié")
    void afficherFormulaireCreation_nonAuthentifie() throws Exception {
        mockMvc.perform(get("/citoyen/getFormIncident"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("Création incident valide")
    @WithMockUser(username = "test@email.com", roles = "CITOYEN")
    void creerIncident_donneesValides() throws Exception {
        mockMvc.perform(post("/citoyen/incident/ajouter")
                        .with(csrf())
                        .param("description", "Incident test valide avec une description suffisamment longue")
                        .param("departementId", String.valueOf(departement.getId()))
                        .param("quartierId", String.valueOf(quartier.getId()))
                        .param("latitude", "36.8065")
                        .param("longitude", "10.1815")
                        .param("priorite", "MOYENNE")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/citoyen/incidents"));
    }

    @Test
    @DisplayName("Création incident avec description trop courte")
    @WithMockUser(username = "test@email.com", roles = "CITOYEN")
    void creerIncident_descriptionTropCourte() throws Exception {
        mockMvc.perform(post("/citoyen/incident/ajouter")
                        .with(csrf())
                        .param("description", "Court")
                        .param("departementId", String.valueOf(departement.getId()))
                        .param("quartierId", String.valueOf(quartier.getId()))
                        .param("latitude", "36.8065")
                        .param("longitude", "10.1815")
                        .param("priorite", "MOYENNE")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("citoyen/incident_form"))
                .andExpect(model().attributeHasFieldErrors("incident", "description"));
    }

    @Test
    @DisplayName("Liste des incidents du citoyen")
    @WithMockUser(username = "test@email.com", roles = "CITOYEN")
    void listerMesIncidents() throws Exception {
        mockMvc.perform(get("/citoyen/incidents"))
                .andExpect(status().isOk())
                .andExpect(view().name("citoyen/incident/liste"))
                .andExpect(model().attributeExists("incidents"));
    }

    @Test
    @DisplayName("Accès refusé pour un AGENT sur les endpoints CITOYEN")
    @WithMockUser(username = "agent@email.com", roles = "AGENT")
    void accesRefuse_AgentVersCitoyen() throws Exception {
        mockMvc.perform(get("/citoyen/getFormIncident"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Page de connexion accessible sans authentification")
    void pageConnexion_Accessible() throws Exception {
        mockMvc.perform(get("/auth/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));
    }
}