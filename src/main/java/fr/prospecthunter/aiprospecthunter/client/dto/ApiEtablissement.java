package fr.prospecthunter.aiprospecthunter.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Le siège de l'entreprise (adresse principale).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiEtablissement(
        String adresse,
        @JsonProperty("code_postal") String codePostal,
        @JsonProperty("libelle_commune") String libelleCommune
) {
}
