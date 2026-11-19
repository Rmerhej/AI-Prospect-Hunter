package fr.prospecthunter.aiprospecthunter.enums;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Départements de recherche. Pour en ajouter un, il suffit d'ajouter une ligne ici.
 */
public enum Departement {

    YVELINES("78", "Yvelines"),
    HAUTS_DE_SEINE("92", "Hauts-de-Seine"),
    PARIS("75", "Paris"),
    VAL_D_OISE("95", "Val-d'Oise");

    /** Zone de la prospection client (inchangée). La zone de la recherche d'emploi se règle dans le profil. */
    public static final List<Departement> ZONE_CLIENT = List.of(YVELINES, HAUTS_DE_SEINE);

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

    public static Optional<Departement> fromCode(String code) {
        return Arrays.stream(values()).filter(d -> d.code.equals(code)).findFirst();
    }

    /** Vrai si le code postal se trouve dans la zone de prospection client (78 ou 92). */
    public static boolean contientCodePostal(String codePostal) {
        return contientCodePostal(codePostal, ZONE_CLIENT);
    }

    /** Vrai si le code postal se trouve dans l'un des départements donnés. */
    public static boolean contientCodePostal(String codePostal, Collection<Departement> departements) {
        if (codePostal == null || departements == null) {
            return false;
        }
        for (Departement departement : departements) {
            if (codePostal.startsWith(departement.code)) {
                return true;
            }
        }
        return false;
    }
}
