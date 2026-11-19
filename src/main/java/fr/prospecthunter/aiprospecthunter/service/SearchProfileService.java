package fr.prospecthunter.aiprospecthunter.service;

import fr.prospecthunter.aiprospecthunter.entity.SearchProfile;
import fr.prospecthunter.aiprospecthunter.repository.SearchProfileRepository;
import org.springframework.stereotype.Service;

/**
 * Lecture et enregistrement de l'unique profil de recherche d'emploi.
 */
@Service
public class SearchProfileService {

    private final SearchProfileRepository repository;

    public SearchProfileService(SearchProfileRepository repository) {
        this.repository = repository;
    }

    /** Le profil enregistré, ou le profil par défaut s'il n'a jamais été enregistré. */
    public SearchProfile get() {
        return repository.findById(SearchProfile.SINGLE_ID).orElseGet(SearchProfile::new);
    }

    public SearchProfile save(SearchProfile profile) {
        // Toujours la même ligne : on ne crée jamais un deuxième profil
        profile.setId(SearchProfile.SINGLE_ID);
        return repository.save(profile);
    }
}
