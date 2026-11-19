package fr.prospecthunter.aiprospecthunter.enums;

import java.util.Arrays;
import java.util.List;

/**
 * Les cibles de recherche et la façon de les trouver dans l'API Recherche d'entreprises.
 * <ul>
 *     <li>Organismes de formation (prospection) : filtre dédié "est_organisme_formation" (plus fiable qu'un code NAF)</li>
 *     <li>Les autres cibles : codes NAF de leur activité principale</li>
 * </ul>
 * Chaque cible appartient à un mode : prospection client ou recherche d'emploi.
 * Pour ajouter une cible, il suffit d'ajouter une ligne ici.
 */
public enum TargetSegment {

    // ----- Prospection client : entreprises de 10 à 49 salariés -----

    FORMATION(
            ProspectMode.CLIENT,
            "Organismes de formation",
            "Formation",
            null,
            true,
            TrancheEffectif.CIBLES,
            "Planning des formateurs et des salles, émargement numérique"),

    RESTAURATION(
            ProspectMode.CLIENT,
            "Restauration",
            "Restauration",
            "56.10A,56.10C,56.21Z",
            false,
            TrancheEffectif.CIBLES,
            "Planning des équipes et gestion des horaires décalés"),

    NETTOYAGE(
            ProspectMode.CLIENT,
            "Nettoyage",
            "Nettoyage",
            "81.21Z,81.22Z",
            false,
            TrancheEffectif.CIBLES,
            "Planning des agents sur plusieurs sites et pointage"),

    AIDE_A_DOMICILE(
            ProspectMode.CLIENT,
            "Aide à domicile",
            "Aide à domicile",
            "88.10A",
            false,
            TrancheEffectif.CIBLES,
            "Tournées des intervenants et suivi des heures"),

    // ----- Recherche d'emploi : employeurs de 10 à 499 salariés -----

    EMPLOI_FORMATION_ADULTES(
            ProspectMode.EMPLOI,
            "Formation d'adultes (poste de formateur)",
            "Formation d'adultes",
            // 85.59A : formation continue d'adultes ; 85.59B : autres enseignements
            "85.59A,85.59B",
            false,
            TrancheEffectif.EMPLOI,
            "Formateur pour adultes (français, savoirs de base, numérique)"),

    EMPLOI_INSERTION(
            ProspectMode.EMPLOI,
            "Insertion et action sociale (poste de formateur)",
            "Insertion / action sociale",
            // 88.99B : action sociale sans hébergement (centres sociaux, insertion) ; 94.99Z : associations
            "88.99B,94.99Z",
            false,
            TrancheEffectif.EMPLOI,
            "Formateur ou animateur d'ateliers de français pour adultes"),

    EMPLOI_INFORMATIQUE(
            ProspectMode.EMPLOI,
            "ESN et éditeurs de logiciels (poste de développeur)",
            "Informatique",
            // 62.01Z : programmation ; 62.02A : conseil en systèmes ; 58.29C : édition de logiciels
            "62.01Z,62.02A,58.29C",
            false,
            TrancheEffectif.EMPLOI,
            "Développeur d'applications Java / Spring Boot");

    private final ProspectMode mode;
    private final String libelle;
    private final String sector;
    private final String codesNaf;
    private final boolean organismeFormation;
    private final String tranches;
    private final String besoinProbable;

    TargetSegment(ProspectMode mode, String libelle, String sector, String codesNaf,
                  boolean organismeFormation, String tranches, String besoinProbable) {
        this.mode = mode;
        this.libelle = libelle;
        this.sector = sector;
        this.codesNaf = codesNaf;
        this.organismeFormation = organismeFormation;
        this.tranches = tranches;
        this.besoinProbable = besoinProbable;
    }

    /** Les cibles d'un mode, dans l'ordre de déclaration. */
    public static List<TargetSegment> ofMode(ProspectMode mode) {
        return Arrays.stream(values()).filter(segment -> segment.mode == mode).toList();
    }

    public ProspectMode getMode() {
        return mode;
    }

    /** Texte affiché dans les listes déroulantes. */
    public String getLibelle() {
        return libelle;
    }

    /** Valeur enregistrée dans Prospect.sector. */
    public String getSector() {
        return sector;
    }

    /** Codes NAF séparés par des virgules, ou null si on utilise le filtre "organisme de formation". */
    public String getCodesNaf() {
        return codesNaf;
    }

    public boolean isOrganismeFormation() {
        return organismeFormation;
    }

    /** Codes INSEE des tranches d'effectif recherchées, séparés par des virgules. */
    public String getTranches() {
        return tranches;
    }

    /** Texte affiché sur la page d'import, par exemple "10 à 49 salariés". */
    public String getEffectifLibelle() {
        return TrancheEffectif.EMPLOI.equals(tranches) ? "10 à 499 salariés" : "10 à 49 salariés";
    }

    /**
     * Hypothèse à confirmer, écrite dans la description du prospect importé :
     * le besoin probable (prospection) ou le poste possible (recherche d'emploi).
     */
    public String getBesoinProbable() {
        return besoinProbable;
    }
}
