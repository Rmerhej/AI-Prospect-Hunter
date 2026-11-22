// Bouton "œil" : afficher / masquer le mot de passe
document.querySelectorAll(".js-toggle-password").forEach(button => {
    button.addEventListener("click", () => {
        const input = document.getElementById(button.dataset.target);
        const visible = input.type === "password";
        input.type = visible ? "text" : "password";
        button.setAttribute("aria-pressed", visible);
        button.setAttribute("aria-label", visible ? "Masquer le mot de passe" : "Afficher le mot de passe");
        button.querySelector("i").className = visible ? "bi bi-eye-slash" : "bi bi-eye";
    });
});