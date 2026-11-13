package fr.prospecthunter.aiprospecthunter.repository;

import fr.prospecthunter.aiprospecthunter.entity.Prospect;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
class ProspectRepositorySirenTest {

    @Autowired
    private ProspectRepository prospectRepository;

    @Test
    void findExistingSirensRenvoieSeulementCeuxEnBase() {
        prospectRepository.save(prospect("Entreprise A", "111111111"));
        prospectRepository.save(prospect("Saisie manuelle", null));

        Set<String> existing = prospectRepository.findExistingSirens(List.of("111111111", "222222222"));

        assertEquals(Set.of("111111111"), existing);
    }

    @Test
    void unMemeSirenNePeutPasEtreEnregistreDeuxFois() {
        prospectRepository.saveAndFlush(prospect("Entreprise A", "111111111"));

        assertThrows(DataIntegrityViolationException.class,
                () -> prospectRepository.saveAndFlush(prospect("Doublon", "111111111")));
    }

    private static Prospect prospect(String name, String siren) {
        Prospect prospect = new Prospect();
        prospect.setName(name);
        prospect.setSiren(siren);
        return prospect;
    }
}
