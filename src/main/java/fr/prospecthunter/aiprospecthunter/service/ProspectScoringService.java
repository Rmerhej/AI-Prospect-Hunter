package fr.prospecthunter.aiprospecthunter.service;

import fr.prospecthunter.aiprospecthunter.entity.Prospect;
import fr.prospecthunter.aiprospecthunter.entity.SearchProfile;
import fr.prospecthunter.aiprospecthunter.enums.Departement;
import fr.prospecthunter.aiprospecthunter.enums.ProspectMode;
import fr.prospecthunter.aiprospecthunter.enums.TargetSegment;
import fr.prospecthunter.aiprospecthunter.enums.TrancheEffectif;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Score "règles simples" (0 à 100), différent selon le mode du prospect.
 *
 * <pre>
 * Prospection client                                      Recherche d'emploi
 * Secteur cible (segments client)                  25     Secteur cible (segments emploi)            25
 * Taille : 10 à 49 salariés = 20, 50 à 99 = 10     20     Taille : 10 à 499 salariés                 15
 * Besoin identifié (champ "besoin" rempli)         20     Poste possible identifié (même champ)      20
 * Zone de prospection (78 ou 92)                   15     Zone choisie dans le profil de recherche   20
 * Présence d'un site web                           10     Présence d'un site web                     10
 * Description d'au moins 50 caractères             10     Description d'au moins 50 caractères       10
 *                                                 ---                                               ---
 *                                                 100                                               100
 * </pre>
 * Un prospect importé obtient environ 70 ("À étudier") : il devient "Prioritaire"
 * quand vous (ou l'IA) avez identifié un vrai besoin ou un poste possible.
 */
@Service
public class ProspectScoringService {

    private static final Set<String> CLIENT_SECTORS = sectorsOf(ProspectMode.CLIENT);
    private static final Set<String> EMPLOI_SECTORS = sectorsOf(ProspectMode.EMPLOI);

    /** Départements visés en recherche d'emploi : lus dans le profil au moment du calcul. */
    private final Supplier<List<Departement>> zoneEmploi;

    @Autowired
    public ProspectScoringService(SearchProfileService profileService) {
        this.zoneEmploi = () -> profileService.get().getDepartementList();
    }

    /** Pour les tests unitaires : la zone de recherche d'emploi est celle du profil par défaut. */
    public ProspectScoringService() {
        List<Departement> defaut = new SearchProfile().getDepartementList();
        this.zoneEmploi = () -> defaut;
    }

    public int calculateScore(Prospect prospect) {
        return prospect.isEmploi() ? scoreEmploi(prospect) : scoreClient(prospect);
    }

    private int scoreClient(Prospect prospect) {
        int score = 0;

        if (prospect.getSector() != null && CLIENT_SECTORS.contains(prospect.getSector())) {
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

        if (Departement.contientCodePostal(prospect.getPostalCode(), Departement.ZONE_CLIENT)) {
            score += 15;
        }

        return score + bonusFiche(prospect);
    }

    private int scoreEmploi(Prospect prospect) {
        int score = 0;

        if (prospect.getSector() != null && EMPLOI_SECTORS.contains(prospect.getSector())) {
            score += 25;
        }

        if (TrancheEffectif.estCibleEmploi(prospect.getTrancheEffectif())) {
            score += 15;
        }

        if (isFilled(prospect.getPotentialNeed())) {
            score += 20;
        }

        if (Departement.contientCodePostal(prospect.getPostalCode(), zoneEmploi.get())) {
            score += 20;
        }

        return score + bonusFiche(prospect);
    }

    /** Points communs aux deux modes : site web et description. */
    private static int bonusFiche(Prospect prospect) {
        int score = 0;
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

    private static Set<String> sectorsOf(ProspectMode mode) {
        return TargetSegment.ofMode(mode).stream()
                .map(TargetSegment::getSector)
                .collect(Collectors.toUnmodifiableSet());
    }

    private static boolean isFilled(String value) {
        return value != null && !value.isBlank();
    }
}
