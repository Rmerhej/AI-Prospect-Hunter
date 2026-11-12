package fr.prospecthunter.aiprospecthunter.service;

import fr.prospecthunter.aiprospecthunter.entity.Prospect;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test unitaire : pas de Spring, pas de base de données, juste la logique de calcul.
 */
class ProspectScoringServiceTest {

    private final ProspectScoringService service = new ProspectScoringService();

    @Test
    void prospectCompletEtCibleObtient100() {
        Prospect prospect = new Prospect();
        prospect.setWebsite("https://www.exemple.fr");
        prospect.setSector("Formation");
        prospect.setPotentialNeed("Planning des formateurs et émargement numérique");
        prospect.setDescription("Organisme de formation de Chatou, 12 formateurs et 3 salles de cours.");
        prospect.setCity("Chatou");

        assertEquals(100, service.calculateScore(prospect));
    }

    @Test
    void prospectVideObtient0() {
        assertEquals(0, service.calculateScore(new Prospect()));
    }

    @Test
    void secteurAutreNeRapportePasDePoints() {
        Prospect prospect = new Prospect();
        prospect.setSector("Autre");

        assertEquals(0, service.calculateScore(prospect));
    }

    @Test
    void descriptionTropCourteNeRapportePasDePoints() {
        Prospect prospect = new Prospect();
        prospect.setDescription("Trop court");

        assertEquals(0, service.calculateScore(prospect));
    }

    @Test
    void qualificationSelonLesSeuils() {
        assertEquals("Prioritaire", service.determineQualification(80));
        assertEquals("À étudier", service.determineQualification(79));
        assertEquals("À étudier", service.determineQualification(50));
        assertEquals("Faible potentiel", service.determineQualification(49));
    }
}
