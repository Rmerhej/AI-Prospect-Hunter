package fr.prospecthunter.aiprospecthunter.enums;

public enum HorizonProjet {

    COURT_TERME("Dans les 3 mois"),
    MOYEN_TERME("Entre 3 et 6 mois"),
    LONG_TERME("Dans plus de 6 mois");

    private final String libelle;

    HorizonProjet(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}