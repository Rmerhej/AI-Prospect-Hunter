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
        prospect.setSector("Formation");
        prospect.setTrancheEffectif("11");
        prospect.setPotentialNeed("Planning des formateurs et émargement numérique");
        prospect.setPostalCode("78400");
        prospect.setWebsite("https://www.exemple.fr");
        prospect.setDescription("Organisme de formation de Chatou, 12 formateurs et 3 salles de cours.");

        assertEquals(100, service.calculateScore(prospect));
    }

    @Test
    void prospectVideObtient0() {
        assertEquals(0, service.calculateScore(new Prospect()));
    }

    @Test
    void secteursAutreEtAssociationNeRapportentPasDePoints() {
        Prospect autre = new Prospect();
        autre.setSector("Autre");
        Prospect association = new Prospect();
        association.setSector("Association");

        assertEquals(0, service.calculateScore(autre));
        assertEquals(0, service.calculateScore(association));
    }

    @Test
    void lesQuatreSegmentsRapportent25Points() {
        for (String sector : new String[]{"Formation", "Restauration", "Nettoyage", "Aide à domicile"}) {
            Prospect prospect = new Prospect();
            prospect.setSector(sector);
            assertEquals(25, service.calculateScore(prospect), sector);
        }
    }

    @Test
    void tailleDeLEntreprise() {
        assertEquals(20, scoreAvecTranche("11"));   // 10-19 salariés
        assertEquals(20, scoreAvecTranche("12"));   // 20-49 salariés
        assertEquals(10, scoreAvecTranche("21"));   // 50-99 salariés
        assertEquals(0, scoreAvecTranche("03"));    // 6-9 salariés
        assertEquals(0, scoreAvecTranche(null));
    }

    @Test
    void zoneDeProspection() {
        assertEquals(15, scoreAvecCodePostal("78400"));   // Yvelines
        assertEquals(15, scoreAvecCodePostal("92100"));   // Hauts-de-Seine
        assertEquals(0, scoreAvecCodePostal("75001"));    // Paris : hors zone
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

    private int scoreAvecTranche(String tranche) {
        Prospect prospect = new Prospect();
        prospect.setTrancheEffectif(tranche);
        return service.calculateScore(prospect);
    }

    private int scoreAvecCodePostal(String codePostal) {
        Prospect prospect = new Prospect();
        prospect.setPostalCode(codePostal);
        return service.calculateScore(prospect);
    }
}
