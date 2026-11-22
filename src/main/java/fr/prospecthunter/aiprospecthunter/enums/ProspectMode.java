package fr.prospecthunter.aiprospecthunter.enums;

/**
 * Les deux usages de l'application :
 * <ul>
 *     <li>CLIENT : prospection commerciale (trouver des entreprises qui ont besoin d'une application)</li>
 *     <li>EMPLOI : recherche d'emploi (trouver des employeurs pour une candidature spontanée)</li>
 * </ul>
 * Le mode d'un prospect décide du calcul du score, du texte envoyé à l'IA et de la signature de l'e-mail.
 */
public enum ProspectMode {

    CLIENT("Prospection client"),
    EMPLOI("Recherche d'emploi");

    private final String libelle;

    ProspectMode(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
