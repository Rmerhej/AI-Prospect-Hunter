package fr.prospecthunter.aiprospecthunter.service;

import fr.prospecthunter.aiprospecthunter.entity.Prospect;
import fr.prospecthunter.aiprospecthunter.repository.ProspectRepository;
import fr.prospecthunter.aiprospecthunter.specification.ProspectSpecifications;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Service
public class ProspectSearchService {

    private final ProspectRepository prospectRepository;

    public ProspectSearchService(ProspectRepository prospectRepository) {
        this.prospectRepository = prospectRepository;
    }

    public Page<Prospect> search(String name, String city, String qualification, int page, int size){

        Specification<Prospect> specification =
                Specification.where(ProspectSpecifications.nameContains(name))
                        .and(ProspectSpecifications.cityContains(city))
                        .and(ProspectSpecifications.hasQualification(qualification));

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Order.desc("score"),
                        Sort.Order.asc("id")
                )
        );

        return prospectRepository.findAll(specification, pageable);
    }
}