package fr.prospecthunter.aiprospecthunter.enums;

import java.util.Map;

/**
 * Codes INSEE des tranches d'effectif salarié et leur libellé.
 */
public final class TrancheEffectif {

    /** Tranches visées : de 10 à 49 salariés. */
    public static final String CIBLES = "11,12";

    private static final Map<String, String> LIBELLES = Map.ofEntries(
            Map.entry("00", "0 salarié"),
            Map.entry("01", "1 à 2 salariés"),
            Map.entry("02", "3 à 5 salariés"),
            Map.entry("03", "6 à 9 salariés"),
            Map.entry("11", "10 à 19 salariés"),
            Map.entry("12", "20 à 49 salariés"),
            Map.entry("21", "50 à 99 salariés"),
            Map.entry("22", "100 à 199 salariés"),
            Map.entry("31", "200 à 249 salariés"),
            Map.entry("32", "250 à 499 salariés"),
            Map.entry("41", "500 à 999 salariés"),
            Map.entry("42", "1 000 à 1 999 salariés"),
            Map.entry("51", "2 000 à 4 999 salariés"),
            Map.entry("52", "5 000 à 9 999 salariés"),
            Map.entry("53", "10 000 salariés et plus")
    );

    private TrancheEffectif() {
        // Classe utilitaire : pas d'instance
    }

    public static String libelle(String code) {
        if (code == null) {
            return "Effectif non renseigné";
        }
        return LIBELLES.getOrDefault(code, "Effectif non renseigné");
    }

    /** Vrai pour les tranches 10-19 et 20-49 salariés. */
    public static boolean estCible(String code) {
        return "11".equals(code) || "12".equals(code);
    }

    /** Vrai pour la tranche 50-99 salariés : encore intéressante, mais moins prioritaire. */
    public static boolean estProche(String code) {
        return "21".equals(code);
    }
}
