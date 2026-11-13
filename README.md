# AI Prospect Hunter

Application web de prospection : identifier, qualifier et suivre des organismes susceptibles d'avoir besoin d'une application sur mesure (planning, présences, émargement…).

Projet personnel d'apprentissage : Spring Boot full stack, puis intégration d'un agent IA.

## Fonctionnalités

- Ajout, modification et suppression de prospects
- Score automatique (0 à 100) et qualification : *Prioritaire*, *À étudier*, *Faible potentiel*
- Recherche multicritère (nom, ville, qualification) avec pagination, via Spring Data JPA Specifications
- Suivi commercial : statut (À contacter → Contacté → Relancé → A répondu → Client / Perdu), financement, horizon du projet
- **Import automatique** depuis l'API publique Recherche d'entreprises : organismes de formation, restauration, nettoyage, aide à domicile, de 10 à 49 salariés, dans les Yvelines et les Hauts-de-Seine. Les doublons sont évités grâce au SIREN.
- **Agent IA (Gemini via Spring AI)** : sur la fiche d'un prospect, l'IA évalue sa pertinence (0 à 100), explique pourquoi, propose un besoin et rédige un brouillon d'e-mail. Vous validez le besoin et corrigez l'e-mail : rien n'est envoyé automatiquement. La signature et la mention d'opposition sont ajoutées par le code.
- Validation des formulaires côté serveur et pages d'erreur claires

## Stack technique

| Couche | Outils |
|---|---|
| Back-end | Java 21, Spring Boot 4, Spring Data JPA, Bean Validation |
| Front-end | Thymeleaf, Bootstrap 5, Bootstrap Icons |
| Base de données | MySQL 8 · H2 pour les tests |
| Déploiement | Docker : image multi-étapes (JDK pour compiler, JRE pour exécuter), Docker Compose |
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
- [ ] Authentification (Spring Security), intégration continue (GitHub Actions)

## Données personnelles

Seules des données publiques d'entreprise sont importées (nom, adresse du siège, activité, effectif). Les noms des dirigeants ne sont pas récupérés. En cas de prospection par e-mail, chaque message doit proposer un moyen simple de s'opposer à la réception de nouveaux messages.

### IA et confidentialité

L'application n'envoie à l'IA que des données publiques d'entreprise (nom, secteur, ville, effectif, code NAF). Avec une offre gratuite, les requêtes peuvent servir à améliorer les modèles du fournisseur : n'y ajoutez jamais de données personnelles ou confidentielles.

Pour changer de fournisseur (Mistral, Ollama…), il suffit de remplacer le starter Spring AI dans `pom.xml` et les lignes `spring.ai` de `application.yml` : le code (`ChatClientProspectAnalyzer`) ne change pas.

## Auteur

Roland Merhej — [LinkedIn](https://www.linkedin.com/in/rolandmerhej)
