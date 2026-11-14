package fr.prospecthunter.aiprospecthunter.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Sécurité de l'application : toutes les pages demandent une connexion.
 * <ul>
 *     <li>Un seul utilisateur, défini dans .env (APP_USERNAME / APP_PASSWORD).</li>
 *     <li>Le mot de passe est haché (BCrypt) en mémoire : il n'est jamais stocké en clair.</li>
 *     <li>Protection CSRF active : Thymeleaf ajoute le jeton caché dans chaque formulaire th:action.</li>
 * </ul>
 */
@Configuration
public class SecurityConfig {

    static final int MIN_PASSWORD_LENGTH = 8;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Pages accessibles sans être connecté
                        .requestMatchers("/login", "/js/**", "/favicon.ico", "/error").permitAll()
                        // Tout le reste exige une connexion
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        // true : toujours revenir à l'accueil après la connexion.
                        // Sinon Spring renvoie vers la dernière adresse demandée avant la connexion,
                        // qui peut être une requête automatique du navigateur (/favicon.ico,
                        // /.well-known/..., /error) au lieu d'une vraie page : d'où une page d'erreur.
                        .defaultSuccessUrl("/", true)
                        .permitAll())
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Encodeur "délégué" : BCrypt par défaut, et prêt pour un changement d'algorithme
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(
            @Value("${prospect-hunter.security.username}") String username,
            @Value("${prospect-hunter.security.password:}") String password,
            PasswordEncoder passwordEncoder) {

        // Pas de mot de passe par défaut : l'application refuse de démarrer sans mot de passe solide
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalStateException(
                    "APP_PASSWORD absent ou trop court (" + MIN_PASSWORD_LENGTH
                            + " caractères minimum) : ajoutez-le dans le fichier .env, puis relancez l'application.");
        }

        return new InMemoryUserDetailsManager(User.withUsername(username)
                .password(passwordEncoder.encode(password))
                .roles("USER")
                .build());
    }
}
