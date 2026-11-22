package fr.prospecthunter.aiprospecthunter.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Réponse de l'API de recherche web Tavily (seuls les champs utiles sont lus).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TavilySearchResponse(List<Result> results) {

    /** Un résultat de recherche : titre, adresse de la page, extrait et pertinence. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Result(String title, String url, String content, Double score) {
    }
}
