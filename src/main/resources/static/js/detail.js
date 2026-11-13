document.addEventListener("DOMContentLoaded", () => {

    // Analyse IA : on désactive le bouton pendant l'appel (quelques secondes), pour éviter les doubles clics
    document.querySelectorAll(".ai-form").forEach(form => {
        form.addEventListener("submit", () => {
            const button = form.querySelector("button");
            button.disabled = true;
            form.querySelector(".label-idle").classList.add("d-none");
            form.querySelector(".label-busy").classList.remove("d-none");
        });
    });

    // Copie de l'objet + du message dans le presse-papiers
    const copyButton = document.getElementById("copy-email");
    if (copyButton) {
        copyButton.addEventListener("click", async () => {
            const subject = document.getElementById("subject").value;
            const body = document.getElementById("body").value;
            const label = copyButton.querySelector("span");
            try {
                await navigator.clipboard.writeText("Objet : " + subject + "\n\n" + body);
                label.textContent = "Copié !";
            } catch (e) {
                label.textContent = "Copie impossible";
            }
            setTimeout(() => label.textContent = "Copier l'e-mail", 2000);
        });
    }
});
