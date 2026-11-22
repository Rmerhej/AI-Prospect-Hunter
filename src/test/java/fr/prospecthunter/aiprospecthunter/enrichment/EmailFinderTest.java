package fr.prospecthunter.aiprospecthunter.enrichment;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Aucun appel réseau : un faux "PageFetcher" sert des pages écrites à la main.
 */
class EmailFinderTest {

    /** Faux site web : adresse -> HTML. Garde la liste des pages demandées. */
    private static final class FakeSite implements PageFetcher {
        private final Map<String, String> pages;
        private final List<String> requested = new ArrayList<>();

        FakeSite(Map<String, String> pages) {
            this.pages = pages;
        }

        @Override
        public Optional<String> fetch(URI uri) {
            requested.add(uri.toString());
            return Optional.ofNullable(pages.get(uri.toString()));
        }
    }

    @Test
    void suitLeLienContactEtPrefereLAdresseDuSite() {
        FakeSite site = new FakeSite(Map.of(
                "https://www.forma-plus.fr", """
                        <a href="/nous-contacter">Contact</a> <a href="/catalogue">Catalogue</a>
                        <p>Webmaster : agence.web@gmail.com</p>
                        """,
                "https://www.forma-plus.fr/nous-contacter", "<p>Écrivez à contact@forma-plus.fr</p>"));

        Optional<FoundEmail> found = new EmailFinder(site).find("https://www.forma-plus.fr", false);

        assertTrue(found.isPresent());
        assertEquals("contact@forma-plus.fr", found.get().email());
        assertEquals("https://www.forma-plus.fr/nous-contacter", found.get().sourceUrl());
        assertFalse(site.requested.contains("https://www.forma-plus.fr/catalogue"), "page inutile non visitée");
    }

    @Test
    void enModeEmploiPrefereLAdresseRecrutement() {
        FakeSite site = new FakeSite(Map.of(
                "https://forma-plus.fr", "<p>contact@forma-plus.fr</p><p>recrutement@forma-plus.fr</p>"));

        assertEquals("recrutement@forma-plus.fr",
                new EmailFinder(site).find("https://forma-plus.fr", true).orElseThrow().email());
        assertEquals("contact@forma-plus.fr",
                new EmailFinder(site).find("https://forma-plus.fr", false).orElseThrow().email());
    }

    @Test
    void sansLienTenteLesPagesHabituelles() {
        FakeSite site = new FakeSite(Map.of(
                "https://forma-plus.fr", "<p>Bienvenue</p>",
                "https://forma-plus.fr/mentions-legales", "<p>Éditeur : direction@forma-plus.fr</p>"));

        assertEquals("direction@forma-plus.fr",
                new EmailFinder(site).find("https://forma-plus.fr", false).orElseThrow().email());
    }

    @Test
    void rienSiAucuneAdresse() {
        FakeSite site = new FakeSite(Map.of("https://forma-plus.fr", "<p>Formulaire de contact uniquement</p>"));
        assertTrue(new EmailFinder(site).find("https://forma-plus.fr", false).isEmpty());
        assertTrue(site.requested.size() <= EmailFinder.MAX_PAGES);
    }

    @Test
    void memeSite() {
        assertTrue(EmailFinder.sameSite("forma-plus.fr", "www.forma-plus.fr"));
        assertTrue(EmailFinder.sameSite("contact.forma-plus.fr", "forma-plus.fr"));
        assertFalse(EmailFinder.sameSite("gmail.com", "forma-plus.fr"));
    }
}
