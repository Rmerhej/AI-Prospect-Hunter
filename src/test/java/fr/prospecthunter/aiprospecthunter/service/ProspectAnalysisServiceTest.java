package fr.prospecthunter.aiprospecthunter.service;

import fr.prospecthunter.aiprospecthunter.ai.AiAnalysis;
import fr.prospecthunter.aiprospecthunter.ai.AiUnavailableException;
import fr.prospecthunter.aiprospecthunter.ai.ProspectAnalyzer;
import fr.prospecthunter.aiprospecthunter.entity.Prospect;
import fr.prospecthunter.aiprospecthunter.exception.ProspectNotFoundException;
import fr.prospecthunter.aiprospecthunter.repository.ProspectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProspectAnalysisServiceTest {

    @Mock
    private ProspectAnalyzer analyzer;

    @Mock
    private ProspectRepository prospectRepository;

    private ProspectAnalysisService service;
    private Prospect prospect;

    @BeforeEach
    void setUp() {
        service = new ProspectAnalysisService(analyzer, prospectRepository, new ProspectScoringService(),
                "Roland Merhej", "Développeur", "Chatou", "https://exemple.fr");

        prospect = new Prospect();
        prospect.setId(1L);
        prospect.setName("FORMA PLUS");
        prospect.setSector("Formation");
        prospect.setTrancheEffectif("11");
        prospect.setPostalCode("78400");
    }

    @Test
    void analyseEnregistreLeResultatEtComposeLEmail() {
        when(prospectRepository.findById(1L)).thenReturn(Optional.of(prospect));
        when(prospectRepository.save(any(Prospect.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(analyzer.analyze(prospect)).thenReturn(new AiAnalysis(
                150, "Analyse détaillée.", "Planning des formateurs", "Votre planning", "Bonjour,\nTexte."));

        Prospect saved = service.analyze(1L);

        assertEquals(100, saved.getAiPertinence(), "La note est bornée à 100");
        assertEquals("Analyse détaillée.", saved.getAiAnalysis());
        assertEquals("Planning des formateurs", saved.getAiProposedNeed());
        assertEquals("Votre planning", saved.getEmailSubject());
        assertNotNull(saved.getAnalysedAt());

        String email = saved.getEmailDraft();
        assertTrue(email.startsWith("Bonjour,"));
        assertTrue(email.contains("Roland Merhej"), "Signature ajoutée");
        assertTrue(email.endsWith(ProspectAnalysisService.OPT_OUT), "Mention d'opposition ajoutée");

        // L'IA propose un besoin, mais il n'est pas appliqué sans votre validation
        assertNull(saved.getPotentialNeed());
    }

    @Test
    void erreurDeLIaNEnregistreRien() {
        when(prospectRepository.findById(1L)).thenReturn(Optional.of(prospect));
        when(analyzer.analyze(prospect)).thenThrow(new AiUnavailableException("indisponible"));

        assertThrows(AiUnavailableException.class, () -> service.analyze(1L));
        verify(prospectRepository, never()).save(any());
    }

    @Test
    void validerLeBesoinRecalculeLeScore() {
        prospect.setAiProposedNeed("Planning des formateurs");
        int scoreAvant = new ProspectScoringService().calculateScore(prospect);
        when(prospectRepository.findById(1L)).thenReturn(Optional.of(prospect));

        assertTrue(service.applyProposedNeed(1L));

        assertEquals("Planning des formateurs", prospect.getPotentialNeed());
        assertEquals(scoreAvant + 20, prospect.getScore());
        verify(prospectRepository).save(prospect);
    }

    @Test
    void sansBesoinProposeRienNeChange() {
        when(prospectRepository.findById(1L)).thenReturn(Optional.of(prospect));

        assertFalse(service.applyProposedNeed(1L));
        verify(prospectRepository, never()).save(any());
    }

    @Test
    void laMentionDOppositionEstRemiseSiElleAEteEffacee() {
        when(prospectRepository.findById(1L)).thenReturn(Optional.of(prospect));

        service.saveDraft(1L, "  Objet  ", "Bonjour,\nTexte modifié.");

        assertEquals("Objet", prospect.getEmailSubject());
        assertTrue(prospect.getEmailDraft().endsWith(ProspectAnalysisService.OPT_OUT));
    }

    @Test
    void prospectInexistant() {
        when(prospectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ProspectNotFoundException.class, () -> service.analyze(99L));
        verifyNoInteractions(analyzer);
    }
}
