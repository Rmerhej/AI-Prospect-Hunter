package fr.prospecthunter.aiprospecthunter.entity;

import fr.prospecthunter.aiprospecthunter.enums.Departement;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Profil de recherche d'emploi : ce que vous cherchez et qui vous êtes.
 * Il n'y a qu'un seul profil (id = 1). Il sert au score des employeurs et à l'analyse IA.
 * Les valeurs par défaut ci-dessous servent tant que le profil n'a pas été enregistré.
 */
@Entity
@Table(name = "search_profile")
public class SearchProfile {

    /** Identifiant fixe : un seul profil par application. */
    public static final long SINGLE_ID = 1L;

    @Id
    private Long id = SINGLE_ID;

    @Column(length = 255)
    private String posteVise =
            "Formateur pour adultes (français, savoirs de base, numérique) en priorité, "
                    + "sinon développeur d'applications Java / Spring Boot";

    @Column(length = 2000)
    private String resumeProfil =
            "Titulaire du titre professionnel Concepteur Développeur d'Applications (CDA). "
                    + "Langues : français et arabe (langues maternelles), anglais professionnel. "
                    + "A développé des applications pour une association de cours de français pour adultes.";

    /** Codes des départements visés, séparés par des virgules (ex : "78,92,75,95"). */
    @Column(length = 100)
    private String departements = "78,92,75,95";

    /** Titre affiché sous votre nom dans la signature des candidatures. */
    @Column(length = 255)
    private String titreSignature = "Formateur pour adultes et développeur d'applications (titre CDA)";

    // ----- Départements : texte en base, liste dans le code -----

    public List<Departement> getDepartementList() {
        if (departements == null || departements.isBlank()) {
            return List.of();
        }
        return Arrays.stream(departements.split(","))
                .map(String::strip)
                .map(Departement::fromCode)
                .flatMap(Optional::stream)
                .distinct()
                .toList();
    }

    public void setDepartementList(List<Departement> list) {
        this.departements = list == null ? "" : list.stream()
                .distinct()
                .map(Departement::getCode)
                .collect(Collectors.joining(","));
    }

    // ----- Accesseurs -----

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getDepartements() {
        return departements;
    }

    public void setDepartements(String departements) {
        this.departements = departements;
    }

    public String getTitreSignature() {
        return titreSignature;
    }

    public void setTitreSignature(String titreSignature) {
        this.titreSignature = titreSignature;
    }
}
