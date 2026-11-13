package fr.prospecthunter.aiprospecthunter.dto;

/**
 * Bilan d'un import.
 *
 * @param imported         entreprises ajoutées à vos prospects
 * @param alreadyPresent   entreprises ignorées car déjà enregistrées
 * @param notFound         SIREN cochés mais absents de la page renvoyée par l'API
 */
public record ImportReport(int imported, int alreadyPresent, int notFound) {

    public String toMessage() {
        StringBuilder message = new StringBuilder(imported + " prospect(s) importé(s).");
        if (alreadyPresent > 0) {
            message.append(" ").append(alreadyPresent).append(" déjà présent(s), ignoré(s).");
        }
        if (notFound > 0) {
            message.append(" ").append(notFound).append(" introuvable(s) dans l'API, ignoré(s).");
        }
        return message.toString();
    }
}
