package fr.prospecthunter.aiprospecthunter.enums;

/**
 * Étapes du suivi commercial d'un prospect.
 */
public enum ProspectStatus {

    A_CONTACTER("À contacter"),
    CONTACTE("Contacté"),
    RELANCE("Relancé"),
    REPONDU("A répondu"),
    CLIENT("Client"),
    PERDU("Perdu");

    private final String libelle;

    ProspectStatus(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
