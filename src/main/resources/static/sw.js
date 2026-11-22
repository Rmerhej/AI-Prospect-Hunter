/*
 * Service worker de Prospect Hunter IA.
 * - Mise en page (JS, icônes, logo, Bootstrap) : servie depuis le cache, l'app s'ouvre vite.
 * - Pages (prospects, tableau de bord…) : TOUJOURS demandées au serveur, jamais mises en cache.
 * - Sans réseau : page "Pas de connexion".
 * Changer VERSION à chaque modification de ce fichier ou des fichiers mis en cache.
 */
const VERSION = "prospect-hunter-v1";
const OFFLINE_URL = "/offline.html";
const PRECACHE = [OFFLINE_URL, "/images/logo.png", "/icons/icon-192.png", "/js/pwa.js", "/manifest.json"];

self.addEventListener("install", event => {
    event.waitUntil(caches.open(VERSION).then(cache => cache.addAll(PRECACHE)));
    self.skipWaiting();
});

self.addEventListener("activate", event => {
    event.waitUntil(
        caches.keys()
            .then(keys => Promise.all(keys.filter(key => key !== VERSION).map(key => caches.delete(key))))
            .then(() => self.clients.claim())
    );
});

function isStatic(url) {
    const sameOrigin = url.origin === self.location.origin && /^\/(js|icons|images)\//.test(url.pathname);
    return sameOrigin || url.hostname === "cdn.jsdelivr.net";
}

self.addEventListener("fetch", event => {
    const request = event.request;
    if (request.method !== "GET") {
        return; // formulaires : jamais interceptés
    }
    if (request.mode === "navigate") {
        event.respondWith(fetch(request).catch(() => caches.match(OFFLINE_URL)));
        return;
    }
    const url = new URL(request.url);
    if (isStatic(url)) {
        event.respondWith(
            caches.open(VERSION).then(cache =>
                cache.match(request).then(cached => {
                    const network = fetch(request).then(response => {
                        if (response.ok || response.type === "opaque") {
                            cache.put(request, response.clone());
                        }
                        return response;
                    }).catch(() => cached);
                    return cached || network;
                })
            )
        );
    }
});
