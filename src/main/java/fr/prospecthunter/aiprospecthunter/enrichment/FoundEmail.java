package fr.prospecthunter.aiprospecthunter.enrichment;

/**
 * Adresse trouvée et page du site où elle est publiée.
 */
public record FoundEmail(String email, String sourceUrl) {
}
