package fr.prospecthunter.aiprospecthunter.client;

import fr.prospecthunter.aiprospecthunter.client.dto.TavilySearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Client de l'API de recherche web Tavily : sert à trouver le site officiel d'une entreprise.
 * Offre gratuite : 1 000 recherches par mois, sans carte bancaire (https://app.tavily.com).
 * Une recherche "basic" coûte 1 crédit. Clé lue dans .env (TAVILY_API_KEY).
 */
@Component
public class TavilyClient {

    private final RestClient restClient;
    private final String apiKey;

    @Autowired
    public TavilyClient(@Value("${prospect-hunter.tavily.base-url:https://api.tavily.com}") String baseUrl,
                        @Value("${prospect-hunter.tavily.api-key:}") String apiKey) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(15));

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.apiKey = apiKey;
    }

    /** Constructeur utilisé par les tests, avec un RestClient simulé. */
    TavilyClient(RestClient restClient, String apiKey) {
        this.restClient = restClient;
        this.apiKey = apiKey;
    }

    /** Vrai si une clé est présente dans .env. */
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    /**
     * Lance une recherche web.
     *
     * @param excludeDomains sites à ignorer (annuaires, réseaux sociaux...)
     */
    public List<TavilySearchResponse.Result> search(String query, List<String> excludeDomains, int maxResults) {
        if (!isConfigured()) {
            throw new ApiUnavailableException(
                    "Recherche de site non configurée : ajoutez TAVILY_API_KEY dans le fichier .env "
                            + "(clé gratuite sur app.tavily.com), puis redémarrez l'application.", null);
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("query", query);
        body.put("search_depth", "basic");
        body.put("max_results", maxResults);
        body.put("exclude_domains", excludeDomains);

        try {
            TavilySearchResponse response = restClient.post()
                    .uri("/search")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(TavilySearchResponse.class);
            return response == null || response.results() == null ? List.of() : response.results();

        } catch (RestClientResponseException e) {
            throw new ApiUnavailableException(messageFor(e.getStatusCode().value()), e);
        } catch (RestClientException e) {
            throw new ApiUnavailableException(
                    "La recherche web (Tavily) ne répond pas pour le moment. Réessayez dans quelques instants.", e);
        }
    }

    static String messageFor(int status) {
        return switch (status) {
            case 401, 403 -> "Clé Tavily refusée : vérifiez TAVILY_API_KEY dans le fichier .env.";
            case 429 -> "Trop de recherches d'affilée : patientez une minute avant de réessayer.";
            case 432, 433 -> "Quota mensuel gratuit de Tavily atteint (1 000 recherches) : "
                    + "il se renouvelle le mois prochain. En attendant, saisissez le site via « Modifier ».";
            default -> "La recherche web (Tavily) a répondu par une erreur (" + status + "). Réessayez plus tard.";
        };
    }
}
