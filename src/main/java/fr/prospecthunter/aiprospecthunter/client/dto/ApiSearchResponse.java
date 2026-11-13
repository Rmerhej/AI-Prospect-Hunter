package fr.prospecthunter.aiprospecthunter.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Réponse de GET /search de l'API Recherche d'entreprises.
 * Seuls les champs utiles sont déclarés ; les autres sont ignorés.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiSearchResponse(
        List<ApiCompany> results,
        @JsonProperty("total_results") Integer totalResults,
        Integer page,
        @JsonProperty("per_page") Integer perPage,
        @JsonProperty("total_pages") Integer totalPages
) {

    /** Évite les NullPointerException si l'API ne renvoie pas de liste. */
    public List<ApiCompany> safeResults() {
        return results == null ? List.of() : results;
    }
}
