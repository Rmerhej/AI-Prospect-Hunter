-- ============================================================
-- V1 : schéma initial de la table "prospects"
-- ------------------------------------------------------------
-- - Base NEUVE : Flyway exécute ce script.
-- - Base EXISTANTE (créée avant par Hibernate) : Flyway ne l'exécute pas,
--   il marque la base "version 1" (baseline-on-migrate dans application.yml).
--
-- Règle d'or : on ne modifie JAMAIS un fichier de migration déjà appliqué.
-- Pour faire évoluer la table, on ajoute V2__description.sql, V3__..., etc.
-- ============================================================

CREATE TABLE IF NOT EXISTS prospects (
    id                  BIGINT        NOT NULL AUTO_INCREMENT,
    name                VARCHAR(150)  NOT NULL,
    website             VARCHAR(255),
    city                VARCHAR(255),
    postal_code         VARCHAR(255),
    sector              VARCHAR(255),
    description         VARCHAR(1000),
    potential_need      VARCHAR(1000),
    created_at          DATETIME(6),
    score               INT,
    qualification       VARCHAR(255),

    -- Suivi commercial (enums Java stockés sous forme de texte)
    status              VARCHAR(20),
    statut_financement  VARCHAR(20)   NOT NULL,
    horizon_projet      VARCHAR(20)   NOT NULL,

    -- Données issues de l'API Recherche d'entreprises
    siren               VARCHAR(9),
    code_naf            VARCHAR(10),
    tranche_effectif    VARCHAR(2),

    -- Analyse IA
    ai_pertinence       INT,
    ai_analysis         VARCHAR(2000),
    ai_proposed_need    VARCHAR(1000),
    email_subject       VARCHAR(200),
    email_draft         VARCHAR(4000),
    analysed_at         DATETIME(6),

    PRIMARY KEY (id),
    CONSTRAINT uk_prospects_siren UNIQUE (siren)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
