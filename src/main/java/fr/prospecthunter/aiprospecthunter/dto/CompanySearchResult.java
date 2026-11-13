package fr.prospecthunter.aiprospecthunter.dto;

import java.util.List;

/**
 * Une page de résultats de l'API, prête à afficher.
 *
 * @param page numéro de page à partir de 1 (convention de l'API)
 */
public record CompanySearchResult(
        List<CompanyCandidate> candidates,
        int page,
        int totalPages,
        int totalResults
) {

    public boolean hasPrevious() {
        return page > 1;
    }

    public boolean hasNext() {
        return page < totalPages;
    }
}
