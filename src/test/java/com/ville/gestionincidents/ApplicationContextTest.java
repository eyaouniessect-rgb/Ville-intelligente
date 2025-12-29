package com.ville.gestionincidents;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test d'intégration minimal
 * Vérifie que le contexte Spring démarre correctement
 */
@SpringBootTest
@ActiveProfiles("test")

class ApplicationContextTest {

    @Test
    void contextLoads() {
        // Si le contexte Spring démarre, le test passe ✅
    }
}
