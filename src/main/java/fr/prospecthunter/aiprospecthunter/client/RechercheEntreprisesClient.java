package fr.prospecthunter.aiprospecthunter.client;

import fr.prospecthunter.aiprospecthunter.client.dto.ApiSearchResponse;
import fr.prospecthunter.aiprospecthunter.enums.Departement;
import fr.prospecthunter.aiprospecthunter.enums.TargetSegment;
import fr.prospecthunter.aiprospecthunter.enums.TrancheEffectif;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Client HTTP de l'API publique Recherche d'entreprises (gratuite, sans clé).
 * Documentation : https://recherche-entreprises.api.gouv.fr/docs/
 */
@Component
public class RechercheEntreprisesClient {

    /** Maximum autorisé par l'API. */
    public static final int PER_PAGE = 25;

    /** L'API ne sert pas plus de 10 000 résultats par recherche, soit 400 pages de 25. */
    public static final int MAX_PAGE = 400;

    private final RestClient restClient;

    @Autowired
    public RechercheEntreprisesClient(
            @Value("${prospect-hunter.api.recherche-entreprises.base-url:https://recherche-entreprises.api.gouv.fr}")
            String baseUrl) {

        // Délais maximum : sans eux, une API lente bloquerait la page indéfiniment
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(10));

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.USER_AGENT, "AI-Prospect-Hunter (projet d'apprentissage)")
                .build();
    }

    /** Constructeur utilisé par les tests, avec un RestClient simulé. */
    RechercheEntreprisesClient(RestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * Recherche les entreprises actives de 10 à 49 salariés d'un segment, dans les départements choisis.
     *
     * @param page numéro de page, à partir de 1 (convention de l'API)
     */
    public ApiSearchResponse search(TargetSegment segment, List<Departement> departements, int page) {

        String codesDepartements = departements.stream()
                .map(Departement::getCode)
                .collect(Collectors.joining(","));

        try {
            return restClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path("/search")
                                .queryParam("departement", codesDepartements)
                                .queryParam("tranche_effectif_salarie", TrancheEffectif.CIBLES)
                                .queryParam("etat_administratif", "A")
                                // Réponse allégée + adresse du siège (sans les dirigeants)
                                .queryParam("minimal", "true")
                                .queryParam("include", "siege")
                                .queryParam("per_page", PER_PAGE)
                                .queryParam("page", page);

                        if (segment.isOrganismeFormation()) {
                            uriBuilder.queryParam("est_organisme_formation", "true");
                        } else {
                            uriBuilder.queryParam("activite_principale", segment.getCodesNaf());
                        }
                        return uriBuilder.build();
                    })
                    .retrieve()
                    .body(ApiSearchResponse.class);

        } catch (RestClientException e) {
            throw new ApiUnavailableException(
                    "L'API Recherche d'entreprises ne répond pas pour le moment. Réessayez dans quelques instants.", e);
        }
    }
}
