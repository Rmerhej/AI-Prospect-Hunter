package fr.prospecthunter.aiprospecthunter.client;

import fr.prospecthunter.aiprospecthunter.client.dto.TavilySearchResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Aucun appel réseau : MockRestServiceServer simule Tavily et vérifie la requête envoyée.
 */
class TavilyClientTest {

    private static final String BASE_URL = "https://tavily.test";
    private static final String KEY = "cle-factice";

    private static final String JSON = """
            {
              "query": "forma plus",
              "results": [
                { "title": "Forma Plus", "url": "https://www.formaplus.fr/", "content": "Organisme", "score": 0.91,
                  "champ_inconnu": "ignoré" }
              ],
              "response_time": 0.8
            }
            """;

    private MockRestServiceServer server;
    private TavilyClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        server = MockRestServiceServer.bindTo(builder).build();
        client = new TavilyClient(builder.build(), KEY);
    }

    @Test
    void envoieLaRechercheEtLitLesResultats() {
        server.expect(requestTo(BASE_URL + "/search"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer " + KEY))
                .andExpect(jsonPath("$.query").value("forma plus"))
                .andExpect(jsonPath("$.search_depth").value("basic"))
                .andExpect(jsonPath("$.max_results").value(5))
                .andExpect(jsonPath("$.exclude_domains[0]").value("societe.com"))
                .andRespond(withSuccess(JSON, MediaType.APPLICATION_JSON));

        List<TavilySearchResponse.Result> results = client.search("forma plus", List.of("societe.com"), 5);

        server.verify();
        assertEquals(1, results.size());
        assertEquals("https://www.formaplus.fr/", results.getFirst().url());
    }

    @Test
    void quotaEpuiseDonneUnMessageClair() {
        server.expect(requestTo(BASE_URL + "/search"))
                .andRespond(withStatus(HttpStatusCode.valueOf(432)));

        ApiUnavailableException e = assertThrows(ApiUnavailableException.class,
                () -> client.search("x", List.of(), 5));
        assertTrue(e.getMessage().contains("Quota mensuel"));
    }

    @Test
    void sansCleOnExpliqueQuoiFaire() {
        TavilyClient sansCle = new TavilyClient(RestClient.builder().baseUrl(BASE_URL).build(), "");
        assertFalse(sansCle.isConfigured());
        ApiUnavailableException e = assertThrows(ApiUnavailableException.class,
                () -> sansCle.search("x", List.of(), 5));
        assertTrue(e.getMessage().contains("TAVILY_API_KEY"));
    }
}
