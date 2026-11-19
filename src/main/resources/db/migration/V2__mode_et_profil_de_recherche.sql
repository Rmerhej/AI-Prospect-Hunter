-- ============================================================
-- V2 : mode "recherche d'emploi" et profil de recherche
-- ------------------------------------------------------------
-- - prospects.prospect_mode : CLIENT (prospection, valeur des prospects existants) ou EMPLOI
-- - search_profile : une seule ligne (id = 1), créée au premier enregistrement de la page Profil
-- ============================================================

ALTER TABLE prospects
    ADD COLUMN prospect_mode VARCHAR(20) NOT NULL DEFAULT 'CLIENT';

CREATE TABLE IF NOT EXISTS search_profile (
    id               BIGINT        NOT NULL,
    poste_vise       VARCHAR(255),
    resume_profil    VARCHAR(2000),
    departements     VARCHAR(100),
    titre_signature  VARCHAR(255),

    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
