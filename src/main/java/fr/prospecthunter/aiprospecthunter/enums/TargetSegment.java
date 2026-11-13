package fr.prospecthunter.aiprospecthunter.enums;

/**
 * Les cibles de prospection et la façon de les trouver dans l'API Recherche d'entreprises.
 * <ul>
 *     <li>Organismes de formation : filtre dédié "est_organisme_formation" (plus fiable qu'un code NAF)</li>
 *     <li>PME à planning : codes NAF de leur activité principale</li>
 * </ul>
 */
public enum TargetSegment {

    FORMATION(
            "Organismes de formation",
            "Formation",
            null,
            true,
            "Planning des formateurs et des salles, émargement numérique"),

    RESTAURATION(
            "Restauration",
            "Restauration",
            "56.10A,56.10C,56.21Z",
            false,
            "Planning des équipes et gestion des horaires décalés"),

    NETTOYAGE(
            "Nettoyage",
            "Nettoyage",
            "81.21Z,81.22Z",
            false,
            "Planning des agents sur plusieurs sites et pointage"),

    AIDE_A_DOMICILE(
            "Aide à domicile",
            "Aide à domicile",
            "88.10A",
            false,
            "Tournées des intervenants et suivi des heures");

    private final String libelle;
    private final String sector;
    private final String codesNaf;
    private final boolean organismeFormation;
    private final String besoinProbable;

    TargetSegment(String libelle, String sector, String codesNaf,
                  boolean organismeFormation, String besoinProbable) {
        this.libelle = libelle;
        this.sector = sector;
        this.codesNaf = codesNaf;
        this.organismeFormation = organismeFormation;
        this.besoinProbable = besoinProbable;
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

    /** Hypothèse de besoin, à confirmer : elle est écrite dans la description du prospect importé. */
    public String getBesoinProbable() {
        return besoinProbable;
    }
}
