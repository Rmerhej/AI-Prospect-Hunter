package fr.prospecthunter.aiprospecthunter.repository;

import fr.prospecthunter.aiprospecthunter.entity.Prospect;
import fr.prospecthunter.aiprospecthunter.specification.ProspectSpecifications;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class ProspectRepositoryTest {

    @Autowired
    private ProspectRepository prospectRepository;

    @Test
    void shouldCombineNameCityAndQualificationFilters() {

        Prospect p1 = new Prospect();
        p1.setName("Secours Catholique Chatou");
        p1.setCity("Chatou");
        p1.setQualification("Prioritaire");
        p1.setScore(90);

        Prospect p2 = new Prospect();
        p2.setName("Secours Catholique Sartrouville");
        p2.setCity("Sartrouville");
        p2.setQualification("Prioritaire");
        p2.setScore(85);

        Prospect p3 = new Prospect();
        p3.setName("Association Chatou");
        p3.setCity("Chatou");
        p3.setQualification("À étudier");
        p3.setScore(60);

        prospectRepository.saveAllAndFlush(List.of(p1, p2, p3));

        Specification<Prospect> specification =
                ProspectSpecifications.nameContains("secours")
                        .and(ProspectSpecifications.cityContains("Chatou"))
                        .and(ProspectSpecifications.hasQualification("Prioritaire"));

        List<Prospect> results =
                prospectRepository.findAll(specification);

        assertEquals(1, results.size());
        assertEquals("Secours Catholique Chatou", results.get(0).getName());
    }
}