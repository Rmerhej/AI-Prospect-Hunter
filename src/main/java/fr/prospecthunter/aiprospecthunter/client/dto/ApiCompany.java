package fr.prospecthunter.aiprospecthunter.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Une entreprise (unité légale) renvoyée par l'API.
 * Les dirigeants (données personnelles) ne sont volontairement PAS récupérés : principe de minimisation du RGPD.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiCompany(
        String siren,
        @JsonProperty("nom_complet") String nomComplet,
        @JsonProperty("activite_principale") String activitePrincipale,
        @JsonProperty("tranche_effectif_salarie") String trancheEffectif,
        @JsonProperty("categorie_entreprise") String categorieEntreprise,
        ApiEtablissement siege
) {
}
