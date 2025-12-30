package com.ville.gestionincidents.controller.incident;

import com.ville.gestionincidents.entity.Departement;
import com.ville.gestionincidents.entity.Quartier;
import com.ville.gestionincidents.entity.Utilisateur;
import com.ville.gestionincidents.enumeration.Role;
import com.ville.gestionincidents.repository.DepartementRepository;
import com.ville.gestionincidents.repository.IncidentRepository;
import com.ville.gestionincidents.repository.NotificationRepository;  // ✅ AJOUTEZ
import com.ville.gestionincidents.repository.PhotoRepository;         // ✅ AJOUTEZ si existe
import com.ville.gestionincidents.repository.QuartierRepository;
import com.ville.gestionincidents.repository.UtilisateurRepository;
import com.ville.gestionincidents.security.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IncidentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DepartementRepository departementRepository;

    @Autowired
    private QuartierRepository quartierRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private NotificationRepository notificationRepository;  // ✅ AJOUTEZ

    // ✅ Si vous avez des photos, ajoutez aussi :
    // @Autowired
    // private PhotoRepository photoRepository;

    @MockBean
    private CurrentUserService currentUserService;

    private Long departementId;
    private Long quartierId;

    @BeforeEach
    void setup() {
        // ✅ ORDRE CRITIQUE : supprimer dans l'ordre inverse des FK
        notificationRepository.deleteAll();  // ✅ 1. D'ABORD les notifications
        // photoRepository.deleteAll();      // ✅ 2. Ensuite les photos (si existe)
        incidentRepository.deleteAll();      // ✅ 3. Puis les incidents
        utilisateurRepository.deleteAll();   // ✅ 4. Puis les utilisateurs
        quartierRepository.deleteAll();      // ✅ 5. Puis les quartiers
        departementRepository.deleteAll();   // ✅ 6. Enfin les départements

        // Créer les données de test
        Departement dep = new Departement();
        dep.setNom("Voirie");
        dep = departementRepository.save(dep);
        departementId = dep.getId();

        Quartier q = new Quartier();
        q.setNom("Centre-ville");
        q.setCodePostal("1000");
        q = quartierRepository.save(q);
        quartierId = q.getId();

        Utilisateur u = new Utilisateur();
        u.setNom("Test");
        u.setEmail("test@test.com");
        u.setRole(Role.CITOYEN);
        utilisateurRepository.save(u);
    }

    @Test
    void testAfficherFormulaire_nonConnecte() throws Exception {
        Mockito.when(currentUserService.getCurrentUser()).thenReturn(null);

        mockMvc.perform(get("/citoyen/getFormIncident"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/auth/login"));
    }

    @Test
    @WithMockUser(username = "test@test.com", roles = "CITOYEN")
    void testAjouterIncident_valide() throws Exception {
        Utilisateur utilisateur = utilisateurRepository.findAll().get(0);
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