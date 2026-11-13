package fr.prospecthunter.aiprospecthunter.ai;

/**
 * Réponse structurée attendue de l'IA.
 * Spring AI demande au modèle de répondre en JSON avec exactement ces champs,
 * puis convertit ce JSON en record Java (méthode .entity(AiAnalysis.class)).
 *
 * @param pertinence      0 à 100 : probabilité que l'entreprise ait besoin d'une app de planning / présences
 * @param analyse         3 à 5 phrases qui expliquent la note
 * @param besoinPotentiel une phrase décrivant le besoin le plus probable
 * @param objetEmail      objet du brouillon d'e-mail
 * @param corpsEmail      corps du brouillon (sans signature ni mention de désinscription)
 */
public record AiAnalysis(
        int pertinence,
        String analyse,
        String besoinPotentiel,
        String objetEmail,
        String corpsEmail
) {
}
