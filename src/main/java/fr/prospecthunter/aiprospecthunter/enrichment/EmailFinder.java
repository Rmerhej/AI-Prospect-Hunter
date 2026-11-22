package fr.prospecthunter.aiprospecthunter.enrichment;

import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Cherche l'adresse de contact publiée sur le site de l'entreprise :
 * page d'accueil, puis pages "contact", "mentions légales", "à propos" (5 pages au plus).
 * En France, les mentions légales sont obligatoires : on y trouve souvent l'adresse.
 */
@Service
public class EmailFinder {

    static final int MAX_PAGES = 5;

    /** Mots qui signalent une page utile, dans l'adresse du lien. */
    private static final List<String> USEFUL_PAGES = List.of(
            "contact", "mentions", "legal", "a-propos", "apropos", "about", "qui-sommes", "equipe",
            "recrutement", "carriere", "emploi", "nous-rejoindre", "jobs");

    /** Pages tentées directement si aucun lien n'a été trouvé sur l'accueil. */
    private static final List<String> FALLBACK_PATHS = List.of("/contact", "/mentions-legales", "/nous-contacter");

    private static final List<String> GENERIC = List.of(
            "contact", "info", "infos", "accueil", "bonjour", "hello", "secretariat", "direction",
            "formation", "commercial", "administration", "agence", "office");

    private static final List<String> JOBS = List.of(
            "rh", "recrutement", "emploi", "jobs", "job", "carriere", "careers", "candidature", "drh");

    private final PageFetcher pageFetcher;

    public EmailFinder(PageFetcher pageFetcher) {
        this.pageFetcher = pageFetcher;
    }

    /**
     * @param website  adresse du site (https://...)
     * @param forJobs  mode recherche d'emploi : on préfère rh@, recrutement@...
     */
    public Optional<FoundEmail> find(String website, boolean forJobs) {
        URI home;
        try {
            home = URI.create(website.trim());
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
        if (home.getHost() == null) {
            return Optional.empty();
        }
        String siteHost = stripWww(home.getHost());

        // adresse -> première page où elle apparaît
        Map<String, String> found = new LinkedHashMap<>();
        Set<URI> visited = new LinkedHashSet<>();

        Optional<String> homeHtml = visit(home, visited, found);
        List<URI> toVisit = new ArrayList<>();
        homeHtml.ifPresent(html -> {
            for (URI link : EmailExtractor.extractLinks(html, home)) {
                if (link.getHost() != null && stripWww(link.getHost()).equals(siteHost) && isUseful(link)) {
                    URI clean = withoutQuery(link);
                    if (!toVisit.contains(clean)) {
                        toVisit.add(clean);
                    }
                }
            }
        });
        if (toVisit.isEmpty()) {
            for (String path : FALLBACK_PATHS) {
                toVisit.add(home.resolve(path));
            }
        }
        for (URI page : toVisit) {
            if (visited.size() >= MAX_PAGES) {
                break;
            }
            visit(page, visited, found);
        }

        return found.entrySet().stream()
                .max((a, b) -> Integer.compare(score(a.getKey(), siteHost, forJobs), score(b.getKey(), siteHost, forJobs)))
                .map(entry -> new FoundEmail(entry.getKey(), entry.getValue()));
    }

    private Optional<String> visit(URI page, Set<URI> visited, Map<String, String> found) {
        if (!visited.add(page)) {
            return Optional.empty();
        }
        Optional<String> html = pageFetcher.fetch(page);
        html.ifPresent(content -> EmailExtractor.extractEmails(content)
                .forEach(email -> found.putIfAbsent(email, page.toString())));
        return html;
    }

    /**
     * Plus le score est haut, meilleure est l'adresse :
     * même nom de domaine que le site (+10), adresse générique de contact (+3),
     * adresse RH en mode emploi (+5). À égalité, la première trouvée l'emporte.
     */
    static int score(String email, String siteHost, boolean forJobs) {
        String local = email.substring(0, email.indexOf('@'));
        String domain = email.substring(email.indexOf('@') + 1);
        int score = 0;
        if (sameSite(domain, siteHost)) {
            score += 10;
        }
        if (GENERIC.contains(local)) {
            score += 3;
        }
        if (forJobs && JOBS.contains(local)) {
            score += 5;
        }
        return score;
    }

    /** exemple.fr et www.exemple.fr / contact.exemple.fr sont le même site. */
    static boolean sameSite(String emailDomain, String siteHost) {
        String a = stripWww(emailDomain);
        String b = stripWww(siteHost);
        return a.equals(b) || a.endsWith("." + b) || b.endsWith("." + a);
    }

    private static boolean isUseful(URI link) {
        String path = link.getPath() == null ? "" : link.getPath().toLowerCase(Locale.ROOT);
        return USEFUL_PAGES.stream().anyMatch(path::contains);
    }

    private static URI withoutQuery(URI uri) {
        try {
            return new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), null, null);
        } catch (Exception e) {
            return uri;
        }
    }

    static String stripWww(String host) {
        String lower = host.toLowerCase(Locale.ROOT);
        return lower.startsWith("www.") ? lower.substring(4) : lower;
    }
}
