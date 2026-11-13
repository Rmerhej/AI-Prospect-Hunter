package fr.prospecthunter.aiprospecthunter.service;

import fr.prospecthunter.aiprospecthunter.client.RechercheEntreprisesClient;
import fr.prospecthunter.aiprospecthunter.client.dto.ApiCompany;
import fr.prospecthunter.aiprospecthunter.client.dto.ApiEtablissement;
import fr.prospecthunter.aiprospecthunter.client.dto.ApiSearchResponse;
import fr.prospecthunter.aiprospecthunter.dto.CompanyCandidate;
import fr.prospecthunter.aiprospecthunter.dto.CompanySearchResult;
import fr.prospecthunter.aiprospecthunter.dto.ImportReport;
import fr.prospecthunter.aiprospecthunter.entity.Prospect;
import fr.prospecthunter.aiprospecthunter.enums.Departement;
import fr.prospecthunter.aiprospecthunter.enums.ProspectStatus;
import fr.prospecthunter.aiprospecthunter.enums.TargetSegment;
import fr.prospecthunter.aiprospecthunter.enums.TrancheEffectif;
import fr.prospecthunter.aiprospecthunter.repository.ProspectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Recherche d'entreprises dans l'API et import des entreprises choisies comme prospects.
 */
@Service
public class CompanyImportService {

    private static final Pattern SIREN_PATTERN = Pattern.compile("^\\d{9}$");

    private final RechercheEntreprisesClient client;
    private final ProspectRepository prospectRepository;
    private final ProspectScoringService scoringService;

    public CompanyImportService(RechercheEntreprisesClient client,
                                ProspectRepository prospectRepository,
                                ProspectScoringService scoringService) {
        this.client = client;
        this.prospectRepository = prospectRepository;
        this.scoringService = scoringService;
    }

    /** Une page de résultats, en signalant les entreprises déjà importées. */
    public CompanySearchResult search(TargetSegment segment, List<Departement> departements, int page) {

        int safePage = clampPage(page);
        ApiSearchResponse response = client.search(segment, departements, safePage);
        List<ApiCompany> companies = response == null ? List.of() : response.safeResults();

        Set<String> existing = findExisting(companies.stream().map(ApiCompany::siren).toList());

        List<CompanyCandidate> candidates = companies.stream()
                .map(company -> toCandidate(company, existing.contains(company.siren())))
                .toList();

        int totalPages = response == null || response.totalPages() == null ? 0 : response.totalPages();
        int totalResults = response == null || response.totalResults() == null ? 0 : response.totalResults();

        return new CompanySearchResult(
                candidates,
                safePage,
                Math.min(totalPages, RechercheEntreprisesClient.MAX_PAGE),
                totalResults);
    }

    /**
     * Importe les entreprises cochées.
     * On ne fait pas confiance aux données renvoyées par le navigateur : on relit la même page
     * dans l'API et on ne garde que les SIREN cochés. Seuls les SIREN viennent du formulaire.
     */
    @Transactional
    public ImportReport importCompanies(TargetSegment segment, List<Departement> departements,
                                        int page, Collection<String> selectedSirens) {

        Set<String> wanted = selectedSirens == null ? Set.of() : selectedSirens.stream()
                .filter(siren -> siren != null && SIREN_PATTERN.matcher(siren).matches())
                .collect(Collectors.toSet());

        if (wanted.isEmpty()) {
            return new ImportReport(0, 0, 0);
        }

        ApiSearchResponse response = client.search(segment, departements, clampPage(page));
        List<ApiCompany> companies = response == null ? List.of() : response.safeResults();

        List<ApiCompany> selected = companies.stream()
                .filter(company -> wanted.contains(company.siren()))
                .toList();

        Set<String> existing = findExisting(selected.stream().map(ApiCompany::siren).toList());

        List<Prospect> toSave = new ArrayList<>();
        for (ApiCompany company : selected) {
            if (!existing.contains(company.siren())) {
                toSave.add(toProspect(company, segment));
            }
        }
        prospectRepository.saveAll(toSave);

        int notFound = wanted.size() - selected.size();
        return new ImportReport(toSave.size(), selected.size() - toSave.size(), notFound);
    }

    // ----- Conversions -----

    private CompanyCandidate toCandidate(ApiCompany company, boolean alreadyImported) {
        ApiEtablissement siege = company.siege();
        return new CompanyCandidate(
                company.siren(),
                company.nomComplet(),
                siege == null ? null : siege.libelleCommune(),
                siege == null ? null : siege.codePostal(),
                company.activitePrincipale(),
                company.trancheEffectif(),
                TrancheEffectif.libelle(company.trancheEffectif()),
                alreadyImported);
    }

    Prospect toProspect(ApiCompany company, TargetSegment segment) {
        ApiEtablissement siege = company.siege();

        Prospect prospect = new Prospect();
        prospect.setSiren(company.siren());
        prospect.setName(truncate(company.nomComplet(), 150));
        prospect.setCity(siege == null ? null : siege.libelleCommune());
        prospect.setPostalCode(siege == null ? null : siege.codePostal());
        prospect.setSector(segment.getSector());
        prospect.setCodeNaf(company.activitePrincipale());
        prospect.setTrancheEffectif(company.trancheEffectif());
        prospect.setStatus(ProspectStatus.A_CONTACTER);

        // Le besoin reste une hypothèse : il va dans la description, pas dans "besoin potentiel".
        // C'est à vous (ou à l'IA plus tard) de le confirmer.
        prospect.setDescription(truncate(
                segment.getLibelle() + " – " + TrancheEffectif.libelle(company.trancheEffectif())
                        + (company.activitePrincipale() == null ? "" : " – NAF " + company.activitePrincipale())
                        + ". Besoin probable : " + segment.getBesoinProbable()
                        + ". Importé depuis l'Annuaire des entreprises.",
                1000));

        int score = scoringService.calculateScore(prospect);
        prospect.setScore(score);
        prospect.setQualification(scoringService.determineQualification(score));
        return prospect;
    }

    // ----- Outils -----

    private Set<String> findExisting(List<String> sirens) {
        List<String> valid = sirens.stream().filter(s -> s != null && !s.isBlank()).toList();
        // Une clause "IN ()" vide n'est pas acceptée par toutes les bases : on évite la requête
        return valid.isEmpty() ? Set.of() : prospectRepository.findExistingSirens(valid);
    }

    private static int clampPage(int page) {
        return Math.min(Math.max(page, 1), RechercheEntreprisesClient.MAX_PAGE);
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}
