package fr.prospecthunter.aiprospecthunter.enrichment;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.util.Optional;

/**
 * Téléchargement prudent des pages d'un site d'entreprise.
 * <ul>
 *     <li>http et https uniquement, délais courts, 1 Mo maximum par page, pages HTML seulement ;</li>
 *     <li>jamais d'adresse interne (localhost, réseau privé, Docker) : l'application ne peut pas être
 *     détournée pour interroger le serveur lui-même (protection "SSRF") ;</li>
 *     <li>redirections suivies à la main (3 au plus), avec la même vérification à chaque étape.</li>
 * </ul>
 */
@Component
public class HttpPageFetcher implements PageFetcher {

    private static final int MAX_BYTES = 1_000_000;
    private static final int MAX_REDIRECTS = 3;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    @Override
    public Optional<String> fetch(URI uri) {
        URI current = uri;
        try {
            for (int i = 0; i <= MAX_REDIRECTS; i++) {
                if (!isAllowed(current)) {
                    return Optional.empty();
                }
                HttpRequest request = HttpRequest.newBuilder(current)
                        .timeout(Duration.ofSeconds(8))
                        .header("User-Agent", "Mozilla/5.0 (compatible; AI-Prospect-Hunter; recherche de contact)")
                        .header("Accept", "text/html")
                        .GET()
                        .build();
                HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

                int status = response.statusCode();
                if (status >= 300 && status < 400) {
                    response.body().close();
                    Optional<String> location = response.headers().firstValue("Location");
                    if (location.isEmpty()) {
                        return Optional.empty();
                    }
                    current = current.resolve(location.get());
                    continue;
                }
                try (InputStream body = response.body()) {
                    String type = response.headers().firstValue("Content-Type").orElse("").toLowerCase(Locale.ROOT);
                    if (status != 200 || !type.contains("html")) {
                        return Optional.empty();
                    }
                    byte[] bytes = body.readNBytes(MAX_BYTES);
                    return Optional.of(new String(bytes, StandardCharsets.UTF_8));
                }
            }
        } catch (IOException | IllegalArgumentException e) {
            return Optional.empty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
        return Optional.empty();
    }

    /** Seules les adresses web publiques sont autorisées. */
    static boolean isAllowed(URI uri) {
        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
        if (!scheme.equals("http") && !scheme.equals("https")) {
            return false;
        }
        if (uri.getHost() == null || uri.getUserInfo() != null) {
            return false;
        }
        int port = uri.getPort();
        if (port != -1 && port != 80 && port != 443) {
            return false;
        }
        try {
            for (InetAddress address : InetAddress.getAllByName(uri.getHost())) {
                if (!isPublic(address)) {
                    return false;
                }
            }
            return true;
        } catch (UnknownHostException e) {
            return false;
        }
    }

    static boolean isPublic(InetAddress address) {
        if (address.isLoopbackAddress() || address.isSiteLocalAddress() || address.isLinkLocalAddress()
                || address.isAnyLocalAddress() || address.isMulticastAddress()) {
            return false;
        }
        byte[] bytes = address.getAddress();
        if (bytes.length == 16 && (bytes[0] & 0xFE) == 0xFC) {
            return false; // IPv6 privée (fc00::/7)
        }
        if (bytes.length == 4 && (bytes[0] & 0xFF) == 100 && (bytes[1] & 0xC0) == 64) {
            return false; // 100.64.0.0/10 (réseau partagé des opérateurs)
        }
        return true;
    }
}
