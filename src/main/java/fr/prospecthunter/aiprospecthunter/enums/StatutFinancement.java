package fr.prospecthunter.aiprospecthunter.enums;

public enum StatutFinancement {

    NON_IDENTIFIE("Non identifié"),
    EN_RECHERCHE("En recherche"),
    IDENTIFIE("Financement identifié");

    private final String libelle;

    StatutFinancement(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}