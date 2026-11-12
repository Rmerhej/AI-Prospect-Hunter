package fr.prospecthunter.aiprospecthunter.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import jakarta.validation.constraints.NotBlank;
import fr.prospecthunter.aiprospecthunter.enums.StatutFinancement;
import fr.prospecthunter.aiprospecthunter.enums.HorizonProjet;
@Entity
@Table(name = "prospects")
public class Prospect {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom de l'organisme est obligatoire")
    @Column(nullable = false, length = 150)
    private String name;

    private String website;

    private String city;

    private String postalCode;

    private String sector;

    @Column(length = 1000)
    private String description;

    @Column(length = 1000)
    private String potentialNeed;

    private LocalDateTime createdAt;
    private Integer score;

    private String qualification;

    private String status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutFinancement statutFinancement = StatutFinancement.NON_IDENTIFIE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HorizonProjet horizonProjet = HorizonProjet.LONG_TERME;
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
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

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
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