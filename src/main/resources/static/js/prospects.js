document.addEventListener("DOMContentLoaded", function () {

    document.querySelectorAll(".delete-form").forEach(form => {

        form.addEventListener("submit", function (event) {

            const confirmation = confirm(
                "Voulez-vous vraiment supprimer ce prospect ?"
            );

            if (!confirmation) {
                event.preventDefault();
            }
        });

    });

});