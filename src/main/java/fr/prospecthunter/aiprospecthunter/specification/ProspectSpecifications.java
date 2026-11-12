package fr.prospecthunter.aiprospecthunter.specification;

import fr.prospecthunter.aiprospecthunter.entity.Prospect;
import org.springframework.data.jpa.domain.Specification;

public class ProspectSpecifications {

    public static Specification<Prospect> nameContains(String name) {
        return (root, query, cb) ->
                name == null || name.isBlank()
                        ? cb.conjunction()
                        :cb.like(
                        cb.lower(root.get("name")),
                        "%" + escapeLike(name.trim().toLowerCase()) + "%",
                        '\\'
                );
    }

    public static Specification<Prospect> cityContains(String city) {
        return (root, query, cb) ->
                city == null || city.isBlank()
                        ? cb.conjunction()
                        : cb.like(
                        cb.lower(root.get("city")),
                        "%" + escapeLike(city.trim().toLowerCase()) + "%",
                        '\\'
                );
    }

    public static Specification<Prospect> hasQualification(String qualification) {
        return (root, query, cb) ->
                qualification == null || qualification.isBlank()
                        ? cb.conjunction()
                        : cb.equal(root.get("qualification"), qualification);
    }
    private static String escapeLike(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}