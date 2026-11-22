package fr.prospecthunter.aiprospecthunter.enrichment;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EmailExtractorTest {

    @Test
    void trouveLesAdressesVisiblesEtMailto() {
        String html = """
                <a href="mailto:Contact%40Forma-Plus.fr?subject=Bonjour">Écrire</a>
                <p>Secrétariat : secretariat@forma-plus.fr</p>
                <p>Encore : contact@forma-plus.fr</p>
                """;
        assertEquals(List.of("contact@forma-plus.fr", "secretariat@forma-plus.fr"),
                List.copyOf(EmailExtractor.extractEmails(html)));
    }

    @Test
    void decodeLesAdressesCachees() {
        assertTrue(EmailExtractor.extractEmails("info [at] forma-plus [dot] fr").contains("info@forma-plus.fr"));
        assertTrue(EmailExtractor.extractEmails("rh&#64;forma-plus.fr").contains("rh@forma-plus.fr"));
        assertTrue(EmailExtractor.extractEmails("accueil (arobase) forma-plus.fr").contains("accueil@forma-plus.fr"));
    }

    @Test
    void ignoreLesFaussesAdresses() {
        String html = """
                <img src="logo@2x.png">
                <script>dsn = "https://abc@sentry.wixpress.com/1"</script>
                <p>noreply@forma-plus.fr</p>
                <p>vous@example.com</p>
                """;
        assertTrue(EmailExtractor.extractEmails(html).isEmpty());
    }

    @Test
    void transformeLesLiensEnAdressesCompletes() {
        String html = """
                <a href="/contact">Contact</a>
                <a href='mentions-legales.html'>Mentions</a>
                <a href="javascript:void(0)">Rien</a>
                <a href="mailto:a@b.fr">Mail</a>
                """;
        List<URI> links = EmailExtractor.extractLinks(html, URI.create("https://www.forma-plus.fr/"));
        assertEquals(List.of(URI.create("https://www.forma-plus.fr/contact"),
                URI.create("https://www.forma-plus.fr/mentions-legales.html")), links);
    }
}
