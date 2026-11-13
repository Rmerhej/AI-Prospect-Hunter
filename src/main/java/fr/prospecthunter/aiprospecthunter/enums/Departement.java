package fr.prospecthunter.aiprospecthunter.enums;

/**
 * Départements de prospection. Pour en ajouter un, il suffit d'ajouter une ligne ici.
 */
public enum Departement {

    YVELINES("78", "Yvelines"),
    HAUTS_DE_SEINE("92", "Hauts-de-Seine");

    private final String code;
    private final String libelle;

    Departement(String code, String libelle) {
        this.code = code;
        this.libelle = libelle;
    }

    public String getCode() {
        return code;
    }

    public String getLibelle() {
        return libelle;
    }

    /** Vrai si le code postal se trouve dans l'un des départements de prospection. */
    public static boolean contientCodePostal(String codePostal) {
        if (codePostal == null) {
            return false;
        }
        for (Departement departement : values()) {
            if (codePostal.startsWith(departement.code)) {
                return true;
            }
        }
        return false;
    }
}
