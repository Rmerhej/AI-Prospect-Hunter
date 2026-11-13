# AI Prospect Hunter

Application web de prospection : identifier, qualifier et suivre des organismes susceptibles d'avoir besoin d'une application sur mesure (planning, présences, émargement…).

Projet personnel d'apprentissage : Spring Boot full stack, puis intégration d'un agent IA.

## Fonctionnalités

- Ajout, modification et suppression de prospects
- Score automatique (0 à 100) et qualification : *Prioritaire*, *À étudier*, *Faible potentiel*
- Recherche multicritère (nom, ville, qualification) avec pagination, via Spring Data JPA Specifications
- Suivi commercial : statut (À contacter → Contacté → Relancé → A répondu → Client / Perdu), financement, horizon du projet
- **Import automatique** depuis l'API publique Recherche d'entreprises : organismes de formation, restauration, nettoyage, aide à domicile, de 10 à 49 salariés, dans les Yvelines et les Hauts-de-Seine. Les doublons sont évités grâce au SIREN.
- Validation des formulaires côté serveur et pages d'erreur claires

## Stack technique

| Couche | Outils |
|---|---|
| Back-end | Java 21, Spring Boot 4, Spring Data JPA, Bean Validation |
| Front-end | Thymeleaf, Bootstrap 5, Bootstrap Icons |
| Base de données | MySQL 8 (Docker) · H2 pour les tests |
| Données | API Recherche d'entreprises (gratuite, sans clé) via `RestClient` |
| Tests | JUnit 5, Mockito, @DataJpaTest, MockRestServiceServer |

## Lancer le projet

Prérequis : Java 21, Docker (par exemple Docker Engine dans WSL).

```bash
# 1. Créer le fichier de secrets (non versionné)
cp .env.example .env
# puis modifier les mots de passe dans .env

# 2. Démarrer MySQL
docker compose up -d

# 3. Lancer l'application
./mvnw spring-boot:run
```

Ouvrir ensuite http://localhost:8080

## Lancer les tests

```bash
./mvnw test
```

Les tests utilisent une base H2 en mémoire : MySQL n'a pas besoin de tourner.

## Feuille de route

- [x] CRUD des prospects, score, statuts
- [x] Recherche multicritère et pagination
- [x] Import automatique d'entreprises via l'API publique Recherche d'entreprises
- [ ] Agent IA (Spring AI) pour analyser les prospects et rédiger des brouillons d'e-mails, validés manuellement
- [ ] Authentification (Spring Security), intégration continue (GitHub Actions)

## Données personnelles

Seules des données publiques d'entreprise sont importées (nom, adresse du siège, activité, effectif). Les noms des dirigeants ne sont pas récupérés. En cas de prospection par e-mail, chaque message doit proposer un moyen simple de s'opposer à la réception de nouveaux messages.

## Auteur

Roland Merhej — [LinkedIn](https://www.linkedin.com/in/rolandmerhej)
