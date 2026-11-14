package fr.prospecthunter.aiprospecthunter.service;

import fr.prospecthunter.aiprospecthunter.dto.DashboardStats;
import fr.prospecthunter.aiprospecthunter.dto.DashboardStats.CountItem;
import fr.prospecthunter.aiprospecthunter.enums.ProspectStatus;
import fr.prospecthunter.aiprospecthunter.repository.ProspectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private ProspectRepository prospectRepository;

    @InjectMocks
    private DashboardService service;

    @Test
    void calculeLesTauxDeReponseEtDeConversion() {
        // 10 prospects : 4 à contacter, 2 contactés, 1 relancé, 1 a répondu, 1 client, 1 sans statut
        when(prospectRepository.countByStatus()).thenReturn(List.of(
                row(ProspectStatus.A_CONTACTER, 4),
                row(ProspectStatus.CONTACTE, 2),
                row(ProspectStatus.RELANCE, 1),
                row(ProspectStatus.REPONDU, 1),
                row(ProspectStatus.CLIENT, 1),
                row(null, 1)));
        when(prospectRepository.countByQualification()).thenReturn(List.of(
                row("Prioritaire", 3), row("À étudier", 7)));
        when(prospectRepository.countBySector()).thenReturn(List.of(
                row("Formation", 6), row("Nettoyage", 3), row(null, 1)));
        when(prospectRepository.countByAnalysedAtIsNotNull()).thenReturn(4L);

        DashboardStats stats = service.compute();

        assertEquals(10, stats.total());
        assertEquals(4, stats.analyses());
        assertEquals(3, stats.prioritaires());
        assertEquals(5, stats.contactes());      // contacté 2 + relancé 1 + répondu 1 + client 1
        assertEquals(2, stats.reponses());       // répondu 1 + client 1
        assertEquals(1, stats.clients());
        assertEquals(40, stats.tauxReponse());   // 2 / 5
        assertEquals(20, stats.tauxConversion()); // 1 / 5

        // Les 6 statuts sont toujours affichés (même à 0), plus "Non défini"
        assertEquals(ProspectStatus.values().length + 1, stats.parStatut().size());
        CountItem perdu = stats.parStatut().stream()
                .filter(item -> item.libelle().equals("Perdu")).findFirst().orElseThrow();
        assertEquals(0, perdu.nombre());

        // Secteurs triés du plus nombreux au moins nombreux, avec pourcentage du total
        assertEquals(new CountItem("Formation", 6, 60), stats.parSecteur().getFirst());
        assertEquals("Non défini", stats.parSecteur().getLast().libelle());
    }

    @Test
    void sansProspectContacteLesTauxSontVides() {
        when(prospectRepository.countByStatus()).thenReturn(List.<Object[]>of(row(ProspectStatus.A_CONTACTER, 3)));
        when(prospectRepository.countByQualification()).thenReturn(List.of());
        when(prospectRepository.countBySector()).thenReturn(List.of());

        DashboardStats stats = service.compute();

        assertEquals(0, stats.contactes());
        assertNull(stats.tauxReponse(), "Pas de taux sur 0 contact");
        assertNull(stats.tauxConversion());
    }

    @Test
    void baseVide() {
        when(prospectRepository.countByStatus()).thenReturn(List.of());
        when(prospectRepository.countByQualification()).thenReturn(List.of());
        when(prospectRepository.countBySector()).thenReturn(List.of());

        DashboardStats stats = service.compute();

        assertEquals(0, stats.total());
        assertTrue(stats.parStatut().stream().allMatch(item -> item.pourcentage() == 0));
    }

    private static Object[] row(Object key, long count) {
        return new Object[]{key, count};
    }
}
