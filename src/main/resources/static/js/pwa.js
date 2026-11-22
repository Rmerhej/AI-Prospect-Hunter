// Application installable (PWA) : enregistrement du service worker
if ("serviceWorker" in navigator) {
    window.addEventListener("load", () => {
        navigator.serviceWorker.register("/sw.js").catch(() => { /* l'app fonctionne aussi sans */ });
    });
}
