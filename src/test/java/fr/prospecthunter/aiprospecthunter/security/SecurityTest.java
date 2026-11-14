package fr.prospecthunter.aiprospecthunter.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.logout;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Teste la sécurité de bout en bout : vraie configuration, vraies pages, base H2.
 * Identifiants de test : voir src/test/resources/application.properties.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void unePageProtegeeRedirigeVersLaConnexion() throws Exception {
        mockMvc.perform(get("/prospects"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void laPageDeConnexionEstAccessible() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Se connecter")));
    }

    @Test
    void connexionAvecLeBonMotDePasse() throws Exception {
        mockMvc.perform(formLogin().user("roland").password("motdepasse-test"))
                .andExpect(authenticated().withUsername("roland"))
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void connexionRefuseeAvecUnMauvaisMotDePasse() throws Exception {
        mockMvc.perform(formLogin().user("roland").password("mauvais"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    @WithMockUser
    void utilisateurConnecteVoitLeTableauDeBord() throws Exception {
        mockMvc.perform(get("/tableau-de-bord"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Taux de réponse")));
    }

    @Test
    @WithMockUser
    void unFormulaireSansJetonCsrfEstRefuse() throws Exception {
        mockMvc.perform(post("/prospects/supprimer/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void avecJetonCsrfLaRequeteEstAcceptee() throws Exception {
        // Le prospect 1 n'existe pas : on obtient la page "introuvable" (404), pas un refus (403)
        mockMvc.perform(post("/prospects/supprimer/1").with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deconnexion() throws Exception {
        mockMvc.perform(logout())
                .andExpect(redirectedUrl("/login?logout"));
    }
}
