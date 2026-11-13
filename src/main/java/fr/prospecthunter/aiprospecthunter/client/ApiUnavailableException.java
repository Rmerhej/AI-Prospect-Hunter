package fr.prospecthunter.aiprospecthunter.client;

/**
 * L'API Recherche d'entreprises n'a pas répondu correctement (panne, délai dépassé, trop de requêtes...).
 */
public class ApiUnavailableException extends RuntimeException {

    public ApiUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
