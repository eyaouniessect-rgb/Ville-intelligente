package com.ville.gestionincidents.controller.incident;

import com.ville.gestionincidents.entity.Departement;
import com.ville.gestionincidents.entity.Quartier;
import com.ville.gestionincidents.entity.Utilisateur;
import com.ville.gestionincidents.repository.DepartementRepository;
import com.ville.gestionincidents.repository.QuartierRepository;
import com.ville.gestionincidents.security.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class IncidentControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private DepartementRepository departementRepository;

    @Autowired
    private QuartierRepository quartierRepository;

    // 🔥 CLÉ DU SUCCÈS
    @MockBean
    private CurrentUserService currentUserService;

    private Long departementId;
    private Long quartierId;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        if (departementRepository.count() == 0) {
            Departement dep = new Departement();
            dep.setNom("Voirie");
            departementRepository.save(dep);
        }

        if (quartierRepository.count() == 0) {
            Quartier q = new Quartier();
            q.setNom("Centre-ville");
            quartierRepository.save(q);
        }

        departementId = departementRepository.findAll().get(0).getId();
        quartierId = quartierRepository.findAll().get(0).getId();
    }

    // ===================== GET =====================
    @Test
    void testAfficherFormulaire_nonConnecte() throws Exception {
        // 🔹 utilisateur NON connecté
        Mockito.when(currentUserService.getCurrentUser()).thenReturn(null);

        mockMvc.perform(get("/citoyen/getFormIncident"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login"));
    }

    // ===================== POST =====================
    @Test
    void testAjouterIncident_valide() throws Exception {
        // 🔹 utilisateur CONNECTÉ
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(1L);
        utilisateur.setNom("Test");

        Mockito.when(currentUserService.getCurrentUser()).thenReturn(utilisateur);

        mockMvc.perform(post("/citoyen/incident/ajouter")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("description", "Rue X sans éclairage public depuis plusieurs jours")
                        .param("departementId", departementId.toString())
                        .param("quartierId", quartierId.toString())
                        .param("latitude", "14.6928")
                        .param("longitude", "-17.4467")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("citoyen/incident_form"))
                .andExpect(model().attributeExists("incident"));
    }
}
