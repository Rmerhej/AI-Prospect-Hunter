package fr.prospecthunter.aiprospecthunter.service;

import fr.prospecthunter.aiprospecthunter.ai.AiAnalysis;
import fr.prospecthunter.aiprospecthunter.ai.ProspectAnalyzer;
import fr.prospecthunter.aiprospecthunter.entity.Prospect;
import fr.prospecthunter.aiprospecthunter.entity.SearchProfile;
import fr.prospecthunter.aiprospecthunter.exception.ProspectNotFoundException;
import fr.prospecthunter.aiprospecthunter.repository.ProspectRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Lance l'analyse IA d'un prospect, enregistre le résultat et gère le brouillon d'e-mail.
 * L'e-mail n'est JAMAIS envoyé automatiquement : c'est vous qui le relisez et l'envoyez.
 * <ul>
 *     <li>Prospection client : signature "développeur" + mention d'opposition (démarchage commercial).</li>
 *     <li>Recherche d'emploi : signature avec le titre du profil, sans mention d'opposition (candidature).</li>
 * </ul>
 */
@Service
public class ProspectAnalysisService {

    /** Mention d'opposition ajoutée par le code (et non par l'IA) : toujours présente en prospection client. */
    static final String OPT_OUT =
            "Si vous ne souhaitez plus recevoir de message de ma part, il vous suffit de répondre « STOP ».";

    private final ProspectAnalyzer analyzer;
    private final ProspectRepository prospectRepository;
    private final ProspectScoringService scoringService;
    private final SearchProfileService profileService;
    private final String nom;
    private final String ville;
    private final String lien;
    private final String signature;

    public ProspectAnalysisService(
            ProspectAnalyzer analyzer,
            ProspectRepository prospectRepository,
            ProspectScoringService scoringService,
            SearchProfileService profileService,
            @Value("${prospect-hunter.signature.nom}") String nom,
            @Value("${prospect-hunter.signature.titre}") String titre,
            @Value("${prospect-hunter.signature.ville}") String ville,
            @Value("${prospect-hunter.signature.lien:}") String lien) {
        this.analyzer = analyzer;
        this.prospectRepository = prospectRepository;
        this.scoringService = scoringService;
        this.profileService = profileService;
        this.nom = nom;
        this.ville = ville;
        this.lien = lien;
        this.signature = buildSignature(nom, titre, ville, lien);
    }

    /**
     * Analyse le prospect et enregistre le résultat.
     * Volontairement SANS @Transactional : l'appel à l'IA peut durer plusieurs secondes,
     * on ne garde pas une connexion à la base ouverte pendant ce temps.
     */
    public Prospect analyze(Long id) {
        Prospect prospect = find(id);
        SearchProfile profile = profileService.get();
        AiAnalysis analysis = analyzer.analyze(prospect, profile);

        prospect.setAiPertinence(Math.clamp(analysis.pertinence(), 0, 100));
        prospect.setAiAnalysis(truncate(analysis.analyse(), 2000));
        prospect.setAiProposedNeed(truncate(analysis.besoinPotentiel(), 1000));
        prospect.setEmailSubject(truncate(analysis.objetEmail(), 200));
        String corps = analysis.corpsEmail();
        String email = prospect.isEmploi()
                ? composeCandidature(corps, profile.getTitreSignature())
                : composeEmail(corps);
        prospect.setEmailDraft(truncate(email, 4000));
        prospect.setAnalysedAt(LocalDateTime.now());

        return prospectRepository.save(prospect);
    }

    /** Vous validez le besoin proposé par l'IA : il devient le "besoin potentiel" et le score est recalculé. */
    public boolean applyProposedNeed(Long id) {
        Prospect prospect = find(id);
        if (prospect.getAiProposedNeed() == null || prospect.getAiProposedNeed().isBlank()) {
            return false;
        }
        prospect.setPotentialNeed(prospect.getAiProposedNeed());
        int score = scoringService.calculateScore(prospect);
        prospect.setScore(score);
        prospect.setQualification(scoringService.determineQualification(score));
        prospectRepository.save(prospect);
        return true;
    }

    /** Enregistre le brouillon après vos corrections. */
    public void saveDraft(Long id, String subject, String body) {
        Prospect prospect = find(id);
        prospect.setEmailSubject(subject.strip());
        // La mention d'opposition ne concerne que le démarchage commercial
        prospect.setEmailDraft(prospect.isEmploi() ? body.strip() : ensureOptOut(body.strip()));
        prospectRepository.save(prospect);
    }

    private Prospect find(Long id) {
        return prospectRepository.findById(id).orElseThrow(() -> new ProspectNotFoundException(id));
    }

    // ----- Composition de l'e-mail -----

    String composeEmail(String corps) {
        return corps.strip() + "\n\n" + signature + "\n\n" + OPT_OUT;
    }

    /** Candidature : signature avec le titre du profil de recherche, sans mention d'opposition. */
    String composeCandidature(String corps, String titreSignature) {
        String titre = titreSignature == null || titreSignature.isBlank()
                ? new SearchProfile().getTitreSignature()
                : titreSignature.strip();
        return corps.strip() + "\n\n" + buildSignature(nom, titre, ville, lien);
    }

    /** Si la mention de désinscription a été effacée pendant la relecture, on la remet. */
    static String ensureOptOut(String body) {
        return body.contains(OPT_OUT) ? body : body + "\n\n" + OPT_OUT;
    }

    private static String buildSignature(String nom, String titre, String ville, String lien) {
        StringBuilder sb = new StringBuilder("Cordialement,\n").append(nom).append("\n").append(titre);
        if (ville != null && !ville.isBlank()) {
            sb.append(" – ").append(ville);
        }
        if (lien != null && !lien.isBlank()) {
            sb.append("\n").append(lien);
        }
        return sb.toString();
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}
