package fr.prospecthunter.aiprospecthunter.enrichment;

import fr.prospecthunter.aiprospecthunter.client.dto.TavilySearchResponse.Result;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WebsiteFinderTest {

    private static Result result(String url) {
        return new Result("titre", url, "extrait", 0.5);
    }

    @Test
    void ecarteLesAnnuairesEtPrefereLeNomDansLeDomaine() {
        List<Result> results = List.of(
                result("https://www.societe.com/societe/forma-plus-123456789.html"),
                result("https://www.blog-formation.fr/article/forma-plus"),
                result("https://www.formaplus.fr/a-propos?ref=x"));

        WebsiteFinder.Website website = WebsiteFinder.choose(results, "SARL Forma'Plus").orElseThrow();

        assertEquals("https://www.formaplus.fr", website.url());
        assertTrue(website.nameInDomain());
    }

    @Test
    void sinonPremierResultatNonAnnuaireAVerifier() {
        List<Result> results = List.of(
                result("https://fr.linkedin.com/company/forma-plus"),
                result("https://www.centre-chatou.fr/"));

        WebsiteFinder.Website website = WebsiteFinder.choose(results, "Forma Plus").orElseThrow();

        assertEquals("https://www.centre-chatou.fr", website.url());
        assertFalse(website.nameInDomain());
    }

    @Test
    void rienQueDesAnnuaires() {
        assertTrue(WebsiteFinder.choose(List.of(result("https://www.pappers.fr/entreprise/x")), "X").isEmpty());
    }

    @Test
    void motsSignificatifs() {
        List<String> words = WebsiteFinder.significantWords("SARL Forma'Plus Chatou");
        assertTrue(words.containsAll(List.of("formaplus", "forma", "plus", "chatou")));
        assertFalse(words.contains("sarl"), "forme juridique ignorée");
    }
}
