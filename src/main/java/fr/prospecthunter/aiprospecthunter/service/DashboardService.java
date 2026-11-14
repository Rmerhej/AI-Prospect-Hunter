package fr.prospecthunter.aiprospecthunter.service;

import fr.prospecthunter.aiprospecthunter.dto.DashboardStats;
import fr.prospecthunter.aiprospecthunter.dto.DashboardStats.CountItem;
import fr.prospecthunter.aiprospecthunter.enums.ProspectStatus;
import fr.prospecthunter.aiprospecthunter.repository.ProspectRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Calcule les chiffres du tableau de bord à partir de quelques requêtes "group by".
 */
@Service
public class DashboardService {

    /** Prospects à qui on a écrit (quelle que soit la suite). */
    static final Set<ProspectStatus> CONTACTES = EnumSet.of(
            ProspectStatus.CONTACTE, ProspectStatus.RELANCE, ProspectStatus.REPONDU,
            ProspectStatus.CLIENT, ProspectStatus.PERDU);

    /** Prospects qui ont répondu. */
    static final Set<ProspectStatus> REPONSES = EnumSet.of(ProspectStatus.REPONDU, ProspectStatus.CLIENT);

    private static final String NON_DEFINI = "Non défini";

    private final ProspectRepository prospectRepository;

    public DashboardService(ProspectRepository prospectRepository) {
        this.prospectRepository = prospectRepository;
    }

    public DashboardStats compute() {
        Map<ProspectStatus, Long> statusCounts = new EnumMap<>(ProspectStatus.class);
        long sansStatut = 0;
        for (Object[] row : prospectRepository.countByStatus()) {
            long count = ((Number) row[1]).longValue();
            if (row[0] instanceof ProspectStatus status) {
                statusCounts.put(status, count);
            } else {
                sansStatut += count;
            }
        }

        long total = statusCounts.values().stream().mapToLong(Long::longValue).sum() + sansStatut;
        long contactes = sum(statusCounts, CONTACTES);
        long reponses = sum(statusCounts, REPONSES);
        long clients = statusCounts.getOrDefault(ProspectStatus.CLIENT, 0L);

        // Tous les statuts, dans l'ordre du suivi commercial, même ceux à 0
        List<CountItem> parStatut = new ArrayList<>();
        for (ProspectStatus status : ProspectStatus.values()) {
            parStatut.add(item(status.getLibelle(), statusCounts.getOrDefault(status, 0L), total));
        }
        if (sansStatut > 0) {
            parStatut.add(item(NON_DEFINI, sansStatut, total));
        }

        long prioritaires = 0;
        List<CountItem> parQualification = new ArrayList<>();
        for (Object[] row : prospectRepository.countByQualification()) {
            long count = ((Number) row[1]).longValue();
            if ("Prioritaire".equals(row[0])) {
                prioritaires = count;
            }
            parQualification.add(item(label(row[0]), count, total));
        }

        List<CountItem> parSecteur = new ArrayList<>();
        for (Object[] row : prospectRepository.countBySector()) {
            parSecteur.add(item(label(row[0]), ((Number) row[1]).longValue(), total));
        }

        // Les plus nombreux en premier
        Comparator<CountItem> parNombre = Comparator.comparingLong(CountItem::nombre).reversed();
        parSecteur.sort(parNombre);
        parQualification.sort(parNombre);

        return new DashboardStats(
                total,
                prospectRepository.countByAnalysedAtIsNotNull(),
                prioritaires,
                contactes,
                reponses,
                clients,
                percentOrNull(reponses, contactes),
                percentOrNull(clients, contactes),
                parStatut,
                parSecteur,
                parQualification);
    }

    // ----- Outils -----

    private static long sum(Map<ProspectStatus, Long> counts, Set<ProspectStatus> statuses) {
        return statuses.stream().mapToLong(s -> counts.getOrDefault(s, 0L)).sum();
    }

    private static CountItem item(String libelle, long nombre, long total) {
        int percent = total == 0 ? 0 : (int) Math.round(nombre * 100.0 / total);
        return new CountItem(libelle, nombre, percent);
    }

    private static String label(Object value) {
        return value == null || value.toString().isBlank() ? NON_DEFINI : value.toString();
    }

    /** Pourcentage arrondi, ou null si le dénominateur est 0 (affiché "—" : un taux sur 0 n'a pas de sens). */
    static Integer percentOrNull(long part, long whole) {
        return whole == 0 ? null : (int) Math.round(part * 100.0 / whole);
    }
}
