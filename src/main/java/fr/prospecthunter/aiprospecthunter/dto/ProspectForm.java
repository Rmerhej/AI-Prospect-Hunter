package fr.prospecthunter.aiprospecthunter.dto;

import fr.prospecthunter.aiprospecthunter.entity.Prospect;
import fr.prospecthunter.aiprospecthunter.enums.HorizonProjet;
import fr.prospecthunter.aiprospecthunter.enums.ProspectStatus;
import fr.prospecthunter.aiprospecthunter.enums.StatutFinancement;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Objet lié au formulaire (DTO).
 * Il ne contient QUE les champs que l'utilisateur a le droit de saisir :
 * score, qualification et createdAt ne peuvent donc pas être injectés via une requête POST.
 */
public class ProspectForm {

    private Long id;

    @NotBlank(message = "Le nom de l'organisme est obligatoire")
    @Size(max = 150, message = "150 caractères maximum")
    private String name;

    @Size(max = 255, message = "255 caractères maximum")
    @Pattern(regexp = "^$|^https?://\\S+$",
            message = "L'adresse doit commencer par http:// ou https://")
    private String website;

    @Size(max = 100, message = "100 caractères maximum")
    private String city;

    @Pattern(regexp = "^$|^\\d{5}$", message = "Le code postal doit contenir 5 chiffres")
    private String postalCode;

    @Size(max = 100)
    private String sector;

    @Size(max = 1000, message = "1000 caractères maximum")
    private String description;

    @Size(max = 1000, message = "1000 caractères maximum")
    private String potentialNeed;

    @NotNull(message = "Choisissez un statut")
    private ProspectStatus status = ProspectStatus.A_CONTACTER;

    @NotNull(message = "Choisissez un statut de financement")
    private StatutFinancement statutFinancement = StatutFinancement.NON_IDENTIFIE;

    @NotNull(message = "Choisissez un horizon")
    private HorizonProjet horizonProjet = HorizonProjet.LONG_TERME;

    /** Prépare le formulaire de modification à partir d'un prospect existant. */
    public static ProspectForm fromEntity(Prospect prospect) {
        ProspectForm form = new ProspectForm();
        form.id = prospect.getId();
        form.name = prospect.getName();
        form.website = prospect.getWebsite();
        form.city = prospect.getCity();
        form.postalCode = prospect.getPostalCode();
        form.sector = prospect.getSector();
        form.description = prospect.getDescription();
        form.potentialNeed = prospect.getPotentialNeed();
        if (prospect.getStatus() != null) {
            form.status = prospect.getStatus();
        }
        if (prospect.getStatutFinancement() != null) {
            form.statutFinancement = prospect.getStatutFinancement();
        }
        if (prospect.getHorizonProjet() != null) {
            form.horizonProjet = prospect.getHorizonProjet();
        }
        return form;
    }

    /** Copie les champs saisis dans l'entité (sans toucher à id, createdAt, score...). */
    public void applyTo(Prospect prospect) {
        prospect.setName(trim(name));
        prospect.setWebsite(trim(website));
        prospect.setCity(trim(city));
        prospect.setPostalCode(trim(postalCode));
        prospect.setSector(trim(sector));
        prospect.setDescription(trim(description));
        prospect.setPotentialNeed(trim(potentialNeed));
        prospect.setStatus(status);
        prospect.setStatutFinancement(statutFinancement);
        prospect.setHorizonProjet(horizonProjet);
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPotentialNeed() {
        return potentialNeed;
    }

    public void setPotentialNeed(String potentialNeed) {
        this.potentialNeed = potentialNeed;
    }

    public ProspectStatus getStatus() {
        return status;
    }

    public void setStatus(ProspectStatus status) {
        this.status = status;
    }

    public StatutFinancement getStatutFinancement() {
        return statutFinancement;
    }

    public void setStatutFinancement(StatutFinancement statutFinancement) {
        this.statutFinancement = statutFinancement;
    }

    public HorizonProjet getHorizonProjet() {
        return horizonProjet;
    }

    public void setHorizonProjet(HorizonProjet horizonProjet) {
        this.horizonProjet = horizonProjet;
    }
}
