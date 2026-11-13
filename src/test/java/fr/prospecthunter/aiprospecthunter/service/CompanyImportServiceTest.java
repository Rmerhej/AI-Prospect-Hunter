package fr.prospecthunter.aiprospecthunter.service;

import fr.prospecthunter.aiprospecthunter.client.RechercheEntreprisesClient;
import fr.prospecthunter.aiprospecthunter.client.dto.ApiCompany;
import fr.prospecthunter.aiprospecthunter.client.dto.ApiEtablissement;
import fr.prospecthunter.aiprospecthunter.client.dto.ApiSearchResponse;
import fr.prospecthunter.aiprospecthunter.dto.CompanySearchResult;
import fr.prospecthunter.aiprospecthunter.dto.ImportReport;
import fr.prospecthunter.aiprospecthunter.entity.Prospect;
import fr.prospecthunter.aiprospecthunter.enums.Departement;
import fr.prospecthunter.aiprospecthunter.enums.ProspectStatus;
import fr.prospecthunter.aiprospecthunter.enums.TargetSegment;
import fr.prospecthunter.aiprospecthunter.repository.ProspectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyImportServiceTest {

    private static final List<Departement> DEPARTEMENTS = List.of(Departement.values());

    @Mock
    private RechercheEntreprisesClient client;

    @Mock
    private ProspectRepository prospectRepository;

    private CompanyImportService service;

    @BeforeEach
    void setUp() {
        // Le vrai service de score : il ne dépend de rien
        service = new CompanyImportService(client, prospectRepository, new ProspectScoringService());
    }

    @Test
    void rechercheSignaleLesEntreprisesDejaImportees() {
        when(client.search(TargetSegment.FORMATION, DEPARTEMENTS, 1))
                .thenReturn(response(company("111111111", "A"), company("222222222", "B")));
        when(prospectRepository.findExistingSirens(anyCollection())).thenReturn(Set.of("222222222"));

        CompanySearchResult result = service.search(TargetSegment.FORMATION, DEPARTEMENTS, 1);

        assertEquals(2, result.candidates().size());
        assertFalse(result.candidates().get(0).alreadyImported());
        assertTrue(result.candidates().get(1).alreadyImported());
        assertEquals("10 à 19 salariés", result.candidates().get(0).trancheLibelle());
    }

    @Test
    void pageInvalideEstCorrigee() {
        when(client.search(any(), any(), anyInt())).thenReturn(response());

        service.search(TargetSegment.FORMATION, DEPARTEMENTS, -5);

        verify(client).search(TargetSegment.FORMATION, DEPARTEMENTS, 1);
    }

    @Test
    void importNeGardeQueLesSirenCochesEtNouveaux() {
        when(client.search(TargetSegment.NETTOYAGE, DEPARTEMENTS, 1))
                .thenReturn(response(
                        company("111111111", "PROPRE 78"),
                        company("222222222", "DEJA LA"),
                        company("333333333", "NON COCHEE")));
        when(prospectRepository.findExistingSirens(anyCollection())).thenReturn(Set.of("222222222"));

        ImportReport report = service.importCompanies(
                TargetSegment.NETTOYAGE, DEPARTEMENTS, 1,
                List.of("111111111", "222222222", "999999999", "pas-un-siren"));

        // 111 importé ; 222 déjà présent ; 999 absent de la page ; "pas-un-siren" rejeté d'office
        assertEquals(new ImportReport(1, 1, 1), report);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Prospect>> captor = ArgumentCaptor.forClass(List.class);
        verify(prospectRepository).saveAll(captor.capture());
        assertEquals(1, captor.getValue().size());

        Prospect saved = captor.getValue().getFirst();
        assertEquals("111111111", saved.getSiren());
        assertEquals("PROPRE 78", saved.getName());
        assertEquals("Nettoyage", saved.getSector());
        assertEquals("CHATOU", saved.getCity());
        assertEquals("78400", saved.getPostalCode());
        assertEquals(ProspectStatus.A_CONTACTER, saved.getStatus());
        assertNull(saved.getPotentialNeed(), "Le besoin reste à confirmer");
        assertTrue(saved.getDescription().contains("Besoin probable"));
        // Secteur 25 + taille 20 + zone 15 + description 10
        assertEquals(70, saved.getScore());
        assertEquals("À étudier", saved.getQualification());
    }

    @Test
    void importSansSelectionNAppellePasLApi() {
        ImportReport report = service.importCompanies(TargetSegment.FORMATION, DEPARTEMENTS, 1, List.of());

        assertEquals(new ImportReport(0, 0, 0), report);
        verifyNoInteractions(client, prospectRepository);
    }

    // ----- Données de test -----

    private static ApiCompany company(String siren, String name) {
        return new ApiCompany(siren, name, "81.21Z", "11", "PME",
                new ApiEtablissement("1 RUE DE PARIS 78400 CHATOU", "78400", "CHATOU"));
    }

    private static ApiSearchResponse response(ApiCompany... companies) {
        return new ApiSearchResponse(List.of(companies), companies.length, 1, 25, 1);
    }
}
