package fr.prospecthunter.aiprospecthunter.entity;

import fr.prospecthunter.aiprospecthunter.enums.HorizonProjet;
import fr.prospecthunter.aiprospecthunter.enums.ProspectStatus;
import fr.prospecthunter.aiprospecthunter.enums.StatutFinancement;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "prospects")
public class Prospect {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    // updatable = false : la date de création n'est jamais réécrite lors d'une modification
    @Column(updatable = false)
    private LocalDateTime createdAt;

    private Integer score;

    private String qualification;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ProspectStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutFinancement statutFinancement = StatutFinancement.NON_IDENTIFIE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private HorizonProjet horizonProjet = HorizonProjet.LONG_TERME;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = ProspectStatus.A_CONTACTER;
        }
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
