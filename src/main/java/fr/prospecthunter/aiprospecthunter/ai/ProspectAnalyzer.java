package fr.prospecthunter.aiprospecthunter.ai;

import fr.prospecthunter.aiprospecthunter.entity.Prospect;
import fr.prospecthunter.aiprospecthunter.entity.SearchProfile;

/**
 * Analyse d'un prospect par une IA.
 * Le reste de l'application ne dépend que de cette interface : passer de Gemini à Mistral, Ollama
 * ou un autre fournisseur ne demandera qu'une nouvelle implémentation.
 */
public interface ProspectAnalyzer {

    /**
     * @param profile profil de recherche d'emploi : utilisé seulement pour un prospect en mode EMPLOI
     * @throws AiUnavailableException si l'IA n'est pas configurée ou ne répond pas
     */
    AiAnalysis analyze(Prospect prospect, SearchProfile profile);
}
