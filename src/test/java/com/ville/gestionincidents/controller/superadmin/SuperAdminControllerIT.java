package com.ville.gestionincidents.controller.superadmin;

import com.ville.gestionincidents.entity.Utilisateur;
import com.ville.gestionincidents.enumeration.Role;
import com.ville.gestionincidents.repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class SuperAdminControllerIT {

    @MockBean
    private CommandLineRunner dataInitializer;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    private Utilisateur superAdmin;
    private Utilisateur userToDelete;

    @BeforeEach
    void setUp() {
        utilisateurRepository.deleteAll();

        // SUPER ADMIN connecté
        superAdmin = new Utilisateur();
        superAdmin.setEmail("admin@test.com");
        superAdmin.setNom("Admin");
        superAdmin.setPrenom("Test");
        superAdmin.setRole(Role.SUPERADMIN);
        superAdmin.setEmailVerifie(true);
        utilisateurRepository.save(superAdmin);

        // UTILISATEUR À SUPPRIMER
        userToDelete = new Utilisateur();
        userToDelete.setEmail("user@test.com");
        userToDelete.setNom("User");
        userToDelete.setPrenom("Delete");
        userToDelete.setRole(Role.ADMIN);
        userToDelete.setEmailVerifie(true);
        utilisateurRepository.save(userToDelete);
    }

    // ================= DASHBOARD =================

    @Test
    @WithMockUser(username = "admin@test.com", roles = "SUPERADMIN")
    void dashboard_shouldReturnDashboardView() throws Exception {
        mockMvc.perform(get("/superadmin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("superadmin/dashboard"))
                .andExpect(model().attributeExists(
                        "totalUsers",
                        "totalAdmins",
                        "totalAgents",
                        "totalCitoyens",
                        "recentUsers"
                ));
    }

    // ================= LIST USERS =================

    @Test
    @WithMockUser(username = "admin@test.com", roles = "SUPERADMIN")
    void listUsers_shouldReturnUsersPage() throws Exception {
        mockMvc.perform(get("/superadmin/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("superadmin/users/users"))
                .andExpect(model().attributeExists("users", "roles"));
    }

    // ================= VIEW USER =================

    @Test
    @WithMockUser(username = "admin@test.com", roles = "SUPERADMIN")
    void viewUser_shouldDisplayUserDetails() throws Exception {
        mockMvc.perform(get("/superadmin/users/{id}", userToDelete.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("superadmin/users/user-details"))
                .andExpect(model().attributeExists("user"));
    }

    // ================= DELETE USER =================

    @Test
    @WithMockUser(username = "admin@test.com", roles = "SUPERADMIN")
    void deleteUser_shouldRemoveUser() throws Exception {
        mockMvc.perform(post("/superadmin/users/{id}/delete", userToDelete.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/superadmin/users"));

        assertThat(utilisateurRepository.findById(userToDelete.getId())).isEmpty();
    }

    // ================= SECURITY =================

    @Test
    @WithMockUser(username = "user@test.com", roles = "ADMIN")
    void accessDenied_ifNotSuperAdmin() throws Exception {
        mockMvc.perform(get("/superadmin/dashboard"))
                // Spring Security → redirection login
                .andExpect(status().is3xxRedirection());
    }
}
