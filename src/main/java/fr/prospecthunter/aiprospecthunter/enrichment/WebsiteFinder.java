package fr.prospecthunter.aiprospecthunter.enrichment;

import fr.prospecthunter.aiprospecthunter.client.TavilyClient;
import fr.prospecthunter.aiprospecthunter.client.dto.TavilySearchResponse;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Trouve le site officiel d'une entreprise avec une recherche web (Tavily).
 * Les annuaires, réseaux sociaux et sites d'emploi sont écartés : on veut le site de l'entreprise.
 */
@Service
public class WebsiteFinder {

    /** Sites qui parlent de l'entreprise sans être le sien. */
    static final List<String> NOT_OFFICIAL = List.of(
            "societe.com", "pappers.fr", "annuaire-entreprises.data.gouv.fr", "data.gouv.fr", "infogreffe.fr",
            "verif.com", "manageo.fr", "pagesjaunes.fr", "kompass.com", "corporama.com", "societeinfo.com",
            "lefigaro.fr", "118712.fr", "mappy.com", "doctrine.fr", "b-reputation.com", "pages-entreprises.com",
            "sirene.fr", "entreprise.data.gouv.fr", "dirigeants.lefigaro.fr", "infonet.fr", "score3.fr",
            "linkedin.com", "facebook.com", "instagram.com", "twitter.com", "x.com", "youtube.com", "tiktok.com",
            "indeed.com", "indeed.fr", "welcometothejungle.com", "glassdoor.fr", "hellowork.com",
            "francetravail.fr", "jobteaser.com", "wikipedia.org", "google.com", "tripadvisor.fr", "yelp.fr");

    /** Mots trop courants pour reconnaître une entreprise dans un nom de domaine. */
    private static final Set<String> STOP_WORDS = Set.of(
            "sarl", "sas", "sasu", "eurl", "sa", "sci", "scop", "association", "asso", "societe", "groupe",
            "group", "france", "the", "les", "des", "and", "pour", "formation", "formations", "services",
            "service", "conseil", "centre", "cabinet", "entreprise", "ets", "etablissements");

    private final TavilyClient tavilyClient;

    public WebsiteFinder(TavilyClient tavilyClient) {
        this.tavilyClient = tavilyClient;
    }

    public boolean isAvailable() {
        return tavilyClient.isConfigured();
    }

    /** Site trouvé : adresse racine, et vrai si le nom de l'entreprise figure dans le nom de domaine. */
    public record Website(String url, boolean nameInDomain) {
    }

    public Optional<Website> find(String companyName, String city) {
        String query = "\"" + companyName + "\" " + (city == null ? "" : city) + " site officiel";
        List<TavilySearchResponse.Result> results = tavilyClient.search(query.trim(), NOT_OFFICIAL, 8);
        return choose(results, companyName);
    }

    /**
     * Choix du résultat : d'abord un site dont le nom de domaine contient un mot du nom de l'entreprise,
     * sinon le premier résultat qui n'est pas un annuaire (marqué "à vérifier").
     */
    static Optional<Website> choose(List<TavilySearchResponse.Result> results, String companyName) {
        List<String> words = significantWords(companyName);
        Website best = null;
        int bestLength = 0;
        Website fallback = null;
        for (TavilySearchResponse.Result result : results) {
            String root = rootUrl(result.url());
            if (root == null) {
                continue;
            }
            String host = URI.create(root).getHost();
            if (isNotOfficial(host)) {
                continue;
            }
            // Le mot le plus long retrouvé dans le domaine : "formaplus" bat "forma" (qui est aussi dans "formation")
            String compactHost = host.replace("-", "").replace(".", "");
            int length = words.stream().filter(compactHost::contains).mapToInt(String::length).max().orElse(0);
            if (length > bestLength) {
                best = new Website(root, true);
                bestLength = length;
            }
            if (fallback == null) {
                fallback = new Website(root, false);
            }
        }
        return Optional.ofNullable(best != null ? best : fallback);
    }

    static boolean isNotOfficial(String host) {
        String h = host.toLowerCase(Locale.ROOT);
        return NOT_OFFICIAL.stream().anyMatch(domain -> h.equals(domain) || h.endsWith("." + domain));
    }

    /** https://www.exemple.fr/page?x=1 -> https://www.exemple.fr */
    static String rootUrl(String url) {
        if (url == null) {
            return null;
        }
        try {
            URI uri = URI.create(url.trim());
            String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
            if (uri.getHost() == null || !(scheme.equals("http") || scheme.equals("https"))) {
                return null;
            }
            return scheme + "://" + uri.getHost().toLowerCase(Locale.ROOT);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** "SARL Forma'Plus Chatou" -> [formaplus, pluschatou, forma, plus, chatou] (sans accents). */
    static List<String> significantWords(String name) {
        if (name == null) {
            return List.of();
        }
        String plain = Normalizer.normalize(name, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
        String[] parts = plain.split("[^a-z0-9]+");
        List<String> kept = Arrays.stream(parts).filter(w -> !w.isEmpty() && !STOP_WORDS.contains(w)).toList();
        List<String> words = new ArrayList<>();
        // Mots collés deux à deux ("forma plus" -> "formaplus"), fréquents dans les noms de domaine
        for (int i = 0; i + 1 < kept.size(); i++) {
            String pair = kept.get(i) + kept.get(i + 1);
            if (pair.length() >= 4 && !words.contains(pair)) {
                words.add(pair);
            }
        }
        for (String word : kept) {
            if (word.length() >= 4 && !words.contains(word)) {
                words.add(word);
            }
        }
        return words;
    }
}
