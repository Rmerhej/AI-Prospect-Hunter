package fr.prospecthunter.aiprospecthunter.service;

import fr.prospecthunter.aiprospecthunter.entity.Prospect;
import org.springframework.stereotype.Service;

@Service
public class ProspectScoringService {

    public int calculateScore(Prospect prospect) {


        int score = 0;

        // Présence d'un site web
        if (prospect.getWebsite() != null
                && !prospect.getWebsite().isBlank()) {
            score += 10;
        }

        // Secteur correspondant à notre cible
        if (prospect.getSector() != null
                && !prospect.getSector().isBlank()) {
            score += 25;
        }

        // Besoin numérique identifié
        if (prospect.getPotentialNeed() != null
                && !prospect.getPotentialNeed().isBlank()) {
            score += 30;
        }

        // Informations suffisamment documentées
        if (prospect.getDescription() != null
                && prospect.getDescription().length() >= 50) {
            score += 15;
        }

        // Potentiel de développement
        if (prospect.getCity() != null
                && !prospect.getCity().isBlank()) {
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
}