package fr.prospecthunter.aiprospecthunter.repository;

import fr.prospecthunter.aiprospecthunter.entity.Prospect;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProspectRepository
        extends JpaRepository<Prospect, Long>,
        JpaSpecificationExecutor<Prospect> {
}