package fr.prospecthunter.aiprospecthunter.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Affiche la page de connexion. La vérification du mot de passe (POST /login)
 * est faite par Spring Security, pas par ce contrôleur.
 */
@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
