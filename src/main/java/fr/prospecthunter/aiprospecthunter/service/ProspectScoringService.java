package fr.prospecthunter.aiprospecthunter.service;

import fr.prospecthunter.aiprospecthunter.entity.Prospect;
import fr.prospecthunter.aiprospecthunter.enums.Departement;
import fr.prospecthunter.aiprospecthunter.enums.TargetSegment;
import fr.prospecthunter.aiprospecthunter.enums.TrancheEffectif;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Score "règles simples" (0 à 100). Il sera complété plus tard par une analyse IA.
 *
 * <pre>
 * Secteur cible (formation, restauration, nettoyage, aide à domicile)  25
 * Taille : 10 à 49 salariés = 20, 50 à 99 = 10                         20
 * Besoin identifié (champ "besoin potentiel" rempli)                   20
 * Zone de prospection (code postal 78 ou 92)                           15
 * Présence d'un site web                                               10
 * Description d'au moins 50 caractères                                 10
 *                                                                     ---
 *                                                                     100
 * </pre>
 * Un prospect importé obtient environ 70 ("À étudier") : il devient "Prioritaire"
 * quand vous (ou l'IA, plus tard) avez identifié un vrai besoin.
 */
@Service
public class ProspectScoringService {

    // Secteurs ciblés = ceux des segments de prospection ("Association" et "Autre" ne rapportent rien)
    private static final Set<String> TARGET_SECTORS = Arrays.stream(TargetSegment.values())
            .map(TargetSegment::getSector)
            .collect(Collectors.toUnmodifiableSet());

    public int calculateScore(Prospect prospect) {
        int score = 0;

        if (prospect.getSector() != null && TARGET_SECTORS.contains(prospect.getSector())) {
            score += 25;
        }

        if (TrancheEffectif.estCible(prospect.getTrancheEffectif())) {
            score += 20;
        } else if (TrancheEffectif.estProche(prospect.getTrancheEffectif())) {
            score += 10;
        }

        if (isFilled(prospect.getPotentialNeed())) {
            score += 20;
        }

        if (Departement.contientCodePostal(prospect.getPostalCode())) {
            score += 15;
        }

        if (isFilled(prospect.getWebsite())) {
            score += 10;
        }

        if (prospect.getDescription() != null && prospect.getDescription().length() >= 50) {
            score += 10;
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
