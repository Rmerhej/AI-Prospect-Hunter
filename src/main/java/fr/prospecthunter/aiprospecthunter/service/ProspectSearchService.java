package fr.prospecthunter.aiprospecthunter.service;

import fr.prospecthunter.aiprospecthunter.entity.Prospect;
import fr.prospecthunter.aiprospecthunter.repository.ProspectRepository;
import fr.prospecthunter.aiprospecthunter.specification.ProspectSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class ProspectSearchService {

    static final int MAX_PAGE_SIZE = 50;

    private final ProspectRepository prospectRepository;

    public ProspectSearchService(ProspectRepository prospectRepository) {
        this.prospectRepository = prospectRepository;
    }

    public Page<Prospect> search(String name, String city, String qualification, int page, int size) {

        Specification<Prospect> specification =
                Specification.where(ProspectSpecifications.nameContains(name))
                        .and(ProspectSpecifications.cityContains(city))
                        .and(ProspectSpecifications.hasQualification(qualification));

        // Les paramètres viennent de l'URL : on les borne.
        // Sans ça, ?page=-1 provoque une erreur 500 et ?size=1000000 charge toute la table.
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);

        Pageable pageable = PageRequest.of(
                safePage,
                safeSize,
                Sort.by(Sort.Order.desc("score"), Sort.Order.asc("id"))
        );

        return prospectRepository.findAll(specification, pageable);
    }
}
