// Case "Tout cocher" de la page d'import
document.addEventListener("DOMContentLoaded", () => {
    const selectAll = document.getElementById("select-all");
    if (!selectAll) {
        return;
    }

    selectAll.addEventListener("change", () => {
        document.querySelectorAll(".company-checkbox:not(:disabled)")
            .forEach(checkbox => checkbox.checked = selectAll.checked);
    });
});
