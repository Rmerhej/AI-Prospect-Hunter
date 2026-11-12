package fr.prospecthunter.aiprospecthunter.exception;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Intercepte les erreurs de tous les contrôleurs et affiche une page claire
 * au lieu de la page blanche "Whitelabel Error Page" (erreur 500).
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProspectNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleProspectNotFound(ProspectNotFoundException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/not-found";
    }

    // Ex : /prospects/modifier/abc ou /prospects?page=abc
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBadParameter(Model model) {
        model.addAttribute("errorMessage", "Paramètre invalide dans l'adresse.");
        return "error/not-found";
    }
}
