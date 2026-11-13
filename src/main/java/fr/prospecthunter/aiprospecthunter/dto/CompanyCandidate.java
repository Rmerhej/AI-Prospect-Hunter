package fr.prospecthunter.aiprospecthunter.dto;

/**
 * Une entreprise trouvée par l'API, affichée dans la page d'import.
 *
 * @param alreadyImported vrai si ce SIREN est déjà dans vos prospects
 */
public record CompanyCandidate(
        String siren,
        String name,
        String city,
        String postalCode,
        String codeNaf,
        String trancheEffectif,
        String trancheLibelle,
        boolean alreadyImported
) {
}
