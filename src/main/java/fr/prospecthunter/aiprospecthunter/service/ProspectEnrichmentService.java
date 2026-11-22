package fr.prospecthunter.aiprospecthunter.service;

import fr.prospecthunter.aiprospecthunter.entity.Prospect;
import fr.prospecthunter.aiprospecthunter.enrichment.EmailFinder;
import fr.prospecthunter.aiprospecthunter.enrichment.FoundEmail;
import fr.prospecthunter.aiprospecthunter.enrichment.WebsiteFinder;
import fr.prospecthunter.aiprospecthunter.exception.ProspectNotFoundException;
import fr.prospecthunter.aiprospecthunter.repository.ProspectRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Complète la fiche d'un prospect : site officiel (recherche web), puis e-mail de contact (sur ce site).
 * Volontairement SANS @Transactional : les appels web peuvent durer plusieurs secondes.
 */
@Service
public class ProspectEnrichmentService {

    private final ProspectRepository prospectRepository;
    private final WebsiteFinder websiteFinder;
    private final EmailFinder emailFinder;

    public ProspectEnrichmentService(ProspectRepository prospectRepository,
                                     WebsiteFinder websiteFinder,
                                     EmailFinder emailFinder) {
        this.prospectRepository = prospectRepository;
        this.websiteFinder = websiteFinder;
        this.emailFinder = emailFinder;
    }

    /** Résultat affiché à l'utilisateur. */
    public record Result(boolean success, String message) {
    }

    /**
     * @throws fr.prospecthunter.aiprospecthunter.client.ApiUnavailableException si la recherche web échoue
     */
    public Result enrich(Long id) {
        Prospect prospect = prospectRepository.findById(id).orElseThrow(() -> new ProspectNotFoundException(id));
        StringBuilder message = new StringBuilder();

        // 1. Site web, s'il manque
        if (isBlank(prospect.getWebsite())) {
            if (!websiteFinder.isAvailable()) {
                return new Result(false, "Site web inconnu. Saisissez-le via « Modifier », ou ajoutez TAVILY_API_KEY "
                        + "dans le fichier .env (clé gratuite sur app.tavily.com) pour le trouver automatiquement.");
            }
            Optional<WebsiteFinder.Website> website = websiteFinder.find(prospect.getName(), prospect.getCity());
            if (website.isEmpty()) {
                prospect.setEnrichedAt(LocalDateTime.now());
                prospectRepository.save(prospect);
                return new Result(false, "Aucun site officiel trouvé pour « " + prospect.getName()
                        + " ». Vous pouvez le saisir via « Modifier ».");
            }
            prospect.setWebsite(website.get().url());
            message.append("Site trouvé : ").append(website.get().url());
            if (!website.get().nameInDomain()) {
                message.append(" (à vérifier : le nom de l'entreprise n'apparaît pas dans l'adresse)");
            }
            message.append(". ");
        }

        // 2. E-mail publié sur le site
        Optional<FoundEmail> email = emailFinder.find(prospect.getWebsite(), prospect.isEmploi());
        if (email.isPresent()) {
            prospect.setEmail(email.get().email());
            prospect.setEmailSource(email.get().sourceUrl());
            message.append("E-mail trouvé : ").append(email.get().email()).append(".");
        } else {
            message.append("Aucune adresse e-mail publiée sur le site : utilisez son formulaire de contact.");
        }

        prospect.setEnrichedAt(LocalDateTime.now());
        prospectRepository.save(prospect);
        return new Result(email.isPresent(), message.toString().trim());
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
