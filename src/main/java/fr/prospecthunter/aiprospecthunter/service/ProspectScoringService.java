package fr.prospecthunter.aiprospecthunter.service;

import fr.prospecthunter.aiprospecthunter.entity.Prospect;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Score "règles simples" (0 à 100).
 * Il sera complété plus tard par une analyse IA.
 */
@Service
public class ProspectScoringService {

    // Secteurs que l'on cible réellement ("Autre" ne rapporte pas de points)
    private static final Set<String> TARGET_SECTORS = Set.of("Association", "Formation");

    public int calculateScore(Prospect prospect) {
        int score = 0;

        // Présence d'un site web
        if (isFilled(prospect.getWebsite())) {
            score += 10;
        }

        // Secteur correspondant à notre cible
        if (prospect.getSector() != null && TARGET_SECTORS.contains(prospect.getSector())) {
            score += 25;
        }

        // Besoin numérique identifié
        if (isFilled(prospect.getPotentialNeed())) {
            score += 30;
        }

        // Informations suffisamment documentées
        if (prospect.getDescription() != null && prospect.getDescription().length() >= 50) {
            score += 15;
        }

        // Localisation connue (utile pour prospecter localement)
        if (isFilled(prospect.getCity())) {
            score += 20;
        }

        return score;
    }

    public String determineQualification(int score) {
        if (score >= 80) {
            return "Prioritaire";
        }
        if (score >= 50) {
            return "À étudier";
        }
        return "Faible potentiel";
    }

    private static boolean isFilled(String value) {
        return value != null && !value.isBlank();
    }
}
