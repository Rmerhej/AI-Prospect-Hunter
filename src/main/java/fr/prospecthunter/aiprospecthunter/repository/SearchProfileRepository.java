package fr.prospecthunter.aiprospecthunter.repository;

import fr.prospecthunter.aiprospecthunter.entity.SearchProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SearchProfileRepository extends JpaRepository<SearchProfile, Long> {
}
