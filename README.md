# AI Prospect Hunter

![icon-192.png](src/main/resources/static/icons/icon-192.png)

[![CI](https://github.com/Rmerhej/AI-Prospect-Hunter/actions/workflows/ci.yml/badge.svg)](https://github.com/Rmerhej/AI-Prospect-Hunter/actions/workflows/ci.yml)

Application web de prospection : identifier, qualifier et suivre des organismes susceptibles d'avoir besoin d'une application sur mesure (planning, présences, émargement…).

Elle a deux usages, choisis prospect par prospect :

- **Prospection client** : trouver des entreprises qui ont besoin d'une application de planning ou de présences.
- **Recherche d'emploi** : trouver des employeurs (formation d'adultes, insertion, informatique) et préparer des candidatures spontanées à partir d'un profil de recherche paramétrable.

Projet personnel d'apprentissage : Spring Boot full stack, puis intégration d'un agent IA.

## Fonctionnalités

- Ajout, modification et suppression de prospects
- Score automatique (0 à 100) et qualification : *Prioritaire*, *À étudier*, *Faible potentiel*
- Recherche multicritère (nom, ville, qualification) avec pagination, via Spring Data JPA Specifications
- Suivi commercial : statut (À contacter → Contacté → Relancé → A répondu → Client / Perdu), financement, horizon du projet
- **Import automatique** depuis l'API publique Recherche d'entreprises. Les doublons sont évités grâce au SIREN.
  - Prospection : organismes de formation, restauration, nettoyage, aide à domicile, de 10 à 49 salariés, dans les Yvelines et les Hauts-de-Seine.
  - Recherche d'emploi : formation d'adultes, insertion et action sociale, ESN et éditeurs de logiciels, de 10 à 499 salariés, dans les départements du profil.
- **Profil de recherche d'emploi** (page « Profil emploi ») : poste visé, résumé du parcours, départements, titre de la signature. Il sert au score des employeurs et à l'analyse IA.
- **Agent IA (Gemini via Spring AI)** : sur la fiche d'un prospect, l'IA évalue sa pertinence (0 à 100), explique pourquoi, propose un besoin et rédige un brouillon d'e-mail. Pour un employeur, elle compare l'entreprise à votre profil, propose un poste possible et rédige une candidature spontanée. Vous validez et corrigez : rien n'est envoyé automatiquement. La signature (et, en prospection, la mention d'opposition) est ajoutée par le code.
- **Tableau de bord** : prospects par statut, secteur et qualification, taux de réponse et de conversion.
- **Connexion sécurisée** (Spring Security) : toutes les pages demandent un identifiant et un mot de passe, protection CSRF sur tous les formulaires.
- Validation des formulaires côté serveur et pages d'erreur claires (y compris la cause des erreurs de l'IA, sans jamais afficher la clé API)

## Stack technique

| Couche | Outils |
|---|---|
| Back-end | Java 21, Spring Boot 4, Spring Data JPA, Bean Validation |
| Front-end | Thymeleaf, Bootstrap 5, Bootstrap Icons |
| Base de données | MySQL 8 · migrations Flyway · H2 pour les tests |
| Sécurité | Spring Security (connexion par formulaire, BCrypt, CSRF) |
| Déploiement | Docker : image multi-étapes (JDK pour compiler, JRE pour exécuter), Docker Compose |
| Intégration continue | GitHub Actions : tests + construction de l'image Docker à chaque push et Pull Request |
| IA | Spring AI 2.0, Gemini (`gemini-2.5-flash`) |
| Données | API Recherche d'entreprises (gratuite, sans clé) via `RestClient` |
| Tests | JUnit 5, Mockito, @DataJpaTest, MockRestServiceServer |

## Lancer le projet

Prérequis : Docker (par exemple Docker Engine dans WSL). Java 21 n'est nécessaire que pour développer.

```bash
# 1. Créer le fichier de secrets (non versionné)
cp .env.example .env
# puis modifier les mots de passe dans .env
# puis ajouter votre clé Gemini (GEMINI_API_KEY), créée sur https://aistudio.google.com/apikey
# et vos identifiants de connexion (APP_USERNAME, APP_PASSWORD : 8 caractères minimum)
```

### Option A : tout dans Docker (application + base)

```bash
docker compose up -d --build     # construit l'image puis démarre MySQL et l'application
docker compose logs -f app       # suivre les logs (Ctrl+C pour quitter)
docker compose down              # tout arrêter (les données MySQL sont conservées)
```

Ouvrir ensuite http://localhost:8080. Après une modification du code, relancer `docker compose up -d --build`.

### Option B : développement (base dans Docker, application dans IntelliJ)

```bash
docker compose up -d prospect-db # uniquement MySQL
./mvnw spring-boot:run           # ou le bouton Run d'IntelliJ
```

Ne lancez pas les deux options en même temps : elles utilisent toutes les deux le port 8080.

## Lancer les tests

```bash
./mvnw test
```

Les tests utilisent une base H2 en mémoire : MySQL n'a pas besoin de tourner.

## Feuille de route

- [x] CRUD des prospects, score, statuts
- [x] Recherche multicritère et pagination
- [x] Import automatique d'entreprises via l'API publique Recherche d'entreprises
- [x] Agent IA (Spring AI + Gemini) pour analyser les prospects et rédiger des brouillons d'e-mails, validés manuellement
- [x] Application et base de données dans Docker (Docker Compose)
- [x] Mode « Recherche d'emploi » avec profil de recherche paramétrable
- [x] Tableau de bord et messages d'erreur IA explicites
- [x] Authentification (Spring Security), migrations (Flyway), intégration continue (GitHub Actions)

## Base de données et migrations

Le schéma est géré par **Flyway** (`src/main/resources/db/migration`) et non plus par Hibernate (`ddl-auto: none`).
Pour faire évoluer une table, ajoutez un nouveau fichier `V2__description.sql`, `V3__…` : ne modifiez jamais une migration déjà appliquée.
Une base créée avant Flyway est automatiquement marquée « version 1 » au premier démarrage.

## Données personnelles

Seules des données publiques d'entreprise sont importées (nom, adresse du siège, activité, effectif). Les noms des dirigeants ne sont pas récupérés. En cas de prospection par e-mail, chaque message doit proposer un moyen simple de s'opposer à la réception de nouveaux messages.

### IA et confidentialité

L'application n'envoie à l'IA que des données publiques d'entreprise (nom, secteur, ville, effectif, code NAF). Avec une offre gratuite, les requêtes peuvent servir à améliorer les modèles du fournisseur : n'y ajoutez jamais de données personnelles ou confidentielles.

Pour changer de fournisseur (Mistral, Ollama…), il suffit de remplacer le starter Spring AI dans `pom.xml` et les lignes `spring.ai` de `application.yml` : le code (`ChatClientProspectAnalyzer`) ne change pas.

## Auteur

Roland Merhej — [LinkedIn](https://www.linkedin.com/in/rolandmerhej)
## License and Copyright

Copyright © 2026 Roland Merhej.
All rights reserved.

Source code is publicly available for review.
See COPYRIGHT.md for usage restrictions.
