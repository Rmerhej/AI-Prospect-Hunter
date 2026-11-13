package fr.prospecthunter.aiprospecthunter.ai;

/**
 * L'IA n'a pas pu produire d'analyse : clé absente, limite atteinte, réponse illisible...
 */
public class AiUnavailableException extends RuntimeException {

    public AiUnavailableException(String message) {
        super(message);
    }

    public AiUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
