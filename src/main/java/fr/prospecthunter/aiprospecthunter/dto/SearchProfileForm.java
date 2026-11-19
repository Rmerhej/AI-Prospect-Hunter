package fr.prospecthunter.aiprospecthunter.dto;

import fr.prospecthunter.aiprospecthunter.entity.SearchProfile;
import fr.prospecthunter.aiprospecthunter.enums.Departement;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

/**
 * Formulaire du profil de recherche d'emploi.
 */
public class SearchProfileForm {

    @NotBlank(message = "Indiquez le poste visé")
    @Size(max = 255, message = "255 caractères maximum")
    private String posteVise;

    @NotBlank(message = "Résumez votre profil : l'IA s'en sert pour rédiger les candidatures")
    @Size(max = 2000, message = "2000 caractères maximum")
    private String resumeProfil;

    @NotEmpty(message = "Choisissez au moins un département")
    private List<Departement> departements = new ArrayList<>();

    @NotBlank(message = "Indiquez le titre de votre signature")
    @Size(max = 255, message = "255 caractères maximum")
    private String titreSignature;

    public static SearchProfileForm fromEntity(SearchProfile profile) {
        SearchProfileForm form = new SearchProfileForm();
        form.posteVise = profile.getPosteVise();
        form.resumeProfil = profile.getResumeProfil();
        form.departements = new ArrayList<>(profile.getDepartementList());
        form.titreSignature = profile.getTitreSignature();
        return form;
    }

    public void applyTo(SearchProfile profile) {
        profile.setPosteVise(posteVise.strip());
        profile.setResumeProfil(resumeProfil.strip());
        profile.setDepartementList(departements);
        profile.setTitreSignature(titreSignature.strip());
    }

    public String getPosteVise() {
        return posteVise;
    }

    public void setPosteVise(String posteVise) {
        this.posteVise = posteVise;
    }

    public String getResumeProfil() {
        return resumeProfil;
    }

    public void setResumeProfil(String resumeProfil) {
        this.resumeProfil = resumeProfil;
    }

    public List<Departement> getDepartements() {
        return departements;
    }

    public void setDepartements(List<Departement> departements) {
        this.departements = departements;
    }

    public String getTitreSignature() {
        return titreSignature;
    }

    public void setTitreSignature(String titreSignature) {
        this.titreSignature = titreSignature;
    }
}
