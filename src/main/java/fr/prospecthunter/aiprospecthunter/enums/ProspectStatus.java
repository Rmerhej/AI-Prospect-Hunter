package fr.prospecthunter.aiprospecthunter.enums;

/**
 * Étapes du suivi d'un prospect (prospection client ou candidature).
 */
public enum ProspectStatus {

    A_CONTACTER("À contacter"),
    CONTACTE("Contacté"),
    RELANCE("Relancé"),
    REPONDU("A répondu"),
    ENTRETIEN("Entretien"),
    CLIENT("Client"),
    EMBAUCHE("Embauché"),
    PERDU("Perdu");

    private final String libelle;

    ProspectStatus(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
