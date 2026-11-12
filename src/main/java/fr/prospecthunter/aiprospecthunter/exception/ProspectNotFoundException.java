package fr.prospecthunter.aiprospecthunter.exception;

public class ProspectNotFoundException extends RuntimeException {

    public ProspectNotFoundException(Long id) {
        super("Le prospect n°" + id + " est introuvable.");
    }
}
