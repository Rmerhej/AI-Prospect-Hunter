package fr.prospecthunter.aiprospecthunter.ai;

import fr.prospecthunter.aiprospecthunter.entity.Prospect;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests sans appel à l'IA : on vérifie ce que l'on ENVOIE à l'IA et les garde-fous.
 */
class ChatClientProspectAnalyzerTest {

    @Test
    void lePromptContientLesDonneesDuProspect() {
        Prospect prospect = new Prospect();
        prospect.setName("FORMA PLUS");
        prospect.setSector("Formation");
        prospect.setCity("CHATOU");
        prospect.setPostalCode("78400");
        prospect.setTrancheEffectif("11");
        prospect.setCodeNaf("85.59A");

        String prompt = ChatClientProspectAnalyzer.buildUserPrompt(prospect);

        assertTrue(prompt.contains("Nom : FORMA PLUS"));
        assertTrue(prompt.contains("Ville : CHATOU (78400)"));
        assertTrue(prompt.contains("Effectif : 10 à 19 salariés"));
        assertTrue(prompt.contains("Code NAF : 85.59A"));
        assertTrue(prompt.contains("Site web : non renseigné"));
    }

    @Test
    void lesAccoladesSontRetireesDesDonnees() {
        // Spring AI interprète { } comme des variables : elles feraient échouer l'appel
        assertEquals("Société (test)", ChatClientProspectAnalyzer.clean("Société {test}"));
        assertEquals("non renseigné", ChatClientProspectAnalyzer.clean("   "));
        assertFalse(ChatClientProspectAnalyzer.SYSTEM_PROMPT.contains("{"));
    }

    @Test
    void cleAbsenteOuParDefautEstDetectee() {
        assertFalse(ChatClientProspectAnalyzer.isConfigured(null));
        assertFalse(ChatClientProspectAnalyzer.isConfigured(""));
        assertFalse(ChatClientProspectAnalyzer.isConfigured(ChatClientProspectAnalyzer.PLACEHOLDER_KEY));
        assertTrue(ChatClientProspectAnalyzer.isConfigured("vraie-cle"));
    }

    @Test
    void sansCleOnNAppellePasLIa() {
        ChatClient.Builder builder = mock(ChatClient.Builder.class);
        ChatClient chatClient = mock(ChatClient.class);
        when(builder.build()).thenReturn(chatClient);

        ChatClientProspectAnalyzer analyzer =
                new ChatClientProspectAnalyzer(builder, ChatClientProspectAnalyzer.PLACEHOLDER_KEY);

        AiUnavailableException e = assertThrows(AiUnavailableException.class,
                () -> analyzer.analyze(new Prospect()));
        assertTrue(e.getMessage().contains("GEMINI_API_KEY"));
        verifyNoInteractions(chatClient);
    }
}
