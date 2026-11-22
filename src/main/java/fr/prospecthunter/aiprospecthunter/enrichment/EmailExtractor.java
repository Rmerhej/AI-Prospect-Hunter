package fr.prospecthunter.aiprospecthunter.enrichment;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lecture d'une page HTML : adresses e-mail visibles et liens.
 * Volontairement simple (expressions régulières, sans bibliothèque) : on cherche seulement
 * les adresses publiées par l'entreprise elle-même (contact, mentions légales).
 */
public final class EmailExtractor {

    private static final Pattern EMAIL =
            Pattern.compile("[A-Za-z0-9._%+-]{1,64}@[A-Za-z0-9-]+(?:\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,24}");

    private static final Pattern HREF =
            Pattern.compile("href\\s*=\\s*[\"']([^\"'#\\s]+)[\"']", Pattern.CASE_INSENSITIVE);

    /** Extensions qui ressemblent à une adresse mais n'en sont pas (ex. logo@2x.png). */
    private static final Pattern FAKE_SUFFIX =
            Pattern.compile(".*\\.(png|jpe?g|gif|webp|svg|css|js|ico|pdf)$");

    /** Domaines techniques ou d'exemple, jamais une vraie adresse de contact. */
    private static final List<String> IGNORED_DOMAINS = List.of(
            "example.com", "example.fr", "exemple.fr", "exemple.com", "domain.com", "domaine.fr",
            "votredomaine.fr", "email.com", "sentry.io", "sentry.wixpress.com", "wixpress.com",
            "sentry-next.wixpress.com", "godaddy.com", "w3.org", "schema.org");

    private EmailExtractor() {
    }

    /** Adresses trouvées dans la page, dans l'ordre d'apparition, sans doublon, en minuscules. */
    public static Set<String> extractEmails(String html) {
        Set<String> emails = new LinkedHashSet<>();
        if (html == null || html.isEmpty()) {
            return emails;
        }
        String text = deobfuscate(html);
        Matcher matcher = EMAIL.matcher(text);
        while (matcher.find()) {
            String email = matcher.group().toLowerCase(Locale.ROOT);
            if (isPlausible(email)) {
                emails.add(email);
            }
        }
        return emails;
    }

    /** Liens de la page, transformés en adresses complètes (http/https uniquement). */
    public static List<URI> extractLinks(String html, URI base) {
        List<URI> links = new ArrayList<>();
        if (html == null) {
            return links;
        }
        Matcher matcher = HREF.matcher(html);
        while (matcher.find()) {
            String href = matcher.group(1).replace("&amp;", "&");
            try {
                URI uri = base.resolve(href);
                if ("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme())) {
                    links.add(uri);
                }
            } catch (IllegalArgumentException e) {
                // lien mal formé : ignoré
            }
        }
        return links;
    }

    /** Décode les écritures courantes destinées à cacher les adresses aux robots. */
    static String deobfuscate(String html) {
        String text = html
                .replace("&#64;", "@").replace("&#x40;", "@").replace("&#X40;", "@").replace("&commat;", "@")
                .replace("&#46;", ".").replace("&#x2e;", ".").replace("&#x2E;", ".").replace("&period;", ".")
                .replaceAll("(?i)\\s*[\\[(]\\s*(at|arobase)\\s*[\\])]\\s*", "@")
                .replaceAll("(?i)\\s*[\\[(]\\s*(dot|point)\\s*[\\])]\\s*", ".");
        // mailto:contact%40exemple.fr
        Matcher mailto = Pattern.compile("(?i)mailto:([^\"'?>\\s]+)").matcher(text);
        StringBuilder decoded = new StringBuilder();
        while (mailto.find()) {
            String address;
            try {
                address = URLDecoder.decode(mailto.group(1), StandardCharsets.UTF_8);
            } catch (IllegalArgumentException e) {
                address = mailto.group(1);
            }
            mailto.appendReplacement(decoded, Matcher.quoteReplacement("mailto:" + address));
        }
        mailto.appendTail(decoded);
        return decoded.toString();
    }

    static boolean isPlausible(String email) {
        if (FAKE_SUFFIX.matcher(email).matches()) {
            return false;
        }
        String domain = email.substring(email.indexOf('@') + 1);
        String local = email.substring(0, email.indexOf('@'));
        if (local.startsWith("noreply") || local.startsWith("no-reply") || local.startsWith("ne-pas-repondre")) {
            return false;
        }
        return IGNORED_DOMAINS.stream().noneMatch(ignored -> domain.equals(ignored) || domain.endsWith("." + ignored));
    }
}
