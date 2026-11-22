package fr.prospecthunter.aiprospecthunter.enrichment;

import java.net.URI;
import java.util.Optional;

/**
 * Télécharge une page web publique. Interface séparée pour que les tests n'aillent pas sur Internet.
 */
public interface PageFetcher {

    /** Contenu HTML de la page, ou vide si elle est inaccessible, trop lourde ou non autorisée. */
    Optional<String> fetch(URI uri);
}
