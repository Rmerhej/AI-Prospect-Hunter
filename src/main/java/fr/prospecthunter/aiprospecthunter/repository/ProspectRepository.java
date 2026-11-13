package fr.prospecthunter.aiprospecthunter.repository;

import fr.prospecthunter.aiprospecthunter.entity.Prospect;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Set;

public interface ProspectRepository
        extends JpaRepository<Prospect, Long>,
        JpaSpecificationExecutor<Prospect> {

    /** Parmi les SIREN donnés, renvoie ceux qui sont déjà enregistrés (une seule requête SQL). */
    @Query("select p.siren from Prospect p where p.siren in :sirens")
    Set<String> findExistingSirens(@Param("sirens") Collection<String> sirens);
}
