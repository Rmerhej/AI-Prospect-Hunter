package fr.prospecthunter.aiprospecthunter.client;

/**
 * Une API externe (Recherche d'entreprises, recherche web Tavily) n'a pas répondu correctement :
 * panne, délai dépassé, clé refusée, quota atteint...
 */
public class ApiUnavailableException extends RuntimeException {

    public ApiUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
