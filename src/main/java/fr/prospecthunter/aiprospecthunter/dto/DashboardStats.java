package fr.prospecthunter.aiprospecthunter.dto;

import java.util.List;

/**
 * Chiffres du tableau de bord, prêts à afficher.
 *
 * @param tauxReponse    pourcentage de prospects contactés qui ont répondu, ou null si personne n'a été contacté
 * @param tauxConversion pourcentage de prospects contactés devenus clients, ou null si personne n'a été contacté
 */
public record DashboardStats(
        long total,
        long analyses,
        long prioritaires,
        long contactes,
        long reponses,
        long clients,
        Integer tauxReponse,
        Integer tauxConversion,
        List<CountItem> parStatut,
        List<CountItem> parSecteur,
        List<CountItem> parQualification
) {

    /**
     * Une ligne d'un graphique en barres.
     *
     * @param pourcentage part du total (0 à 100), pour la largeur de la barre
     */
    public record CountItem(String libelle, long nombre, int pourcentage) {
    }
}
