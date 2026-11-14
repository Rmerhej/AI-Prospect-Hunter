package fr.prospecthunter.aiprospecthunter.repository;

import fr.prospecthunter.aiprospecthunter.entity.Prospect;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface ProspectRepository
        extends JpaRepository<Prospect, Long>,
        JpaSpecificationExecutor<Prospect> {

    /** Parmi les SIREN donnés, renvoie ceux qui sont déjà enregistrés (une seule requête SQL). */
    @Query("select p.siren from Prospect p where p.siren in :sirens")
    Set<String> findExistingSirens(@Param("sirens") Collection<String> sirens);

    // ----- Statistiques du tableau de bord : une ligne [valeur, nombre] par groupe -----

    @Query("select p.status, count(p) from Prospect p group by p.status")
    List<Object[]> countByStatus();

    @Query("select p.sector, count(p) from Prospect p group by p.sector")
    List<Object[]> countBySector();

    @Query("select p.qualification, count(p) from Prospect p group by p.qualification")
    List<Object[]> countByQualification();

    long countByAnalysedAtIsNotNull();
}
