-- ============================================================
-- V3 : coordonnées trouvées automatiquement
-- ------------------------------------------------------------
-- - email        : adresse de contact trouvée sur le site (ou saisie à la main)
-- - email_source : page du site où l'adresse a été trouvée (pour vérifier)
-- - enriched_at  : date de la dernière recherche automatique
-- ============================================================

ALTER TABLE prospects
    ADD COLUMN email        VARCHAR(254) NULL,
    ADD COLUMN email_source VARCHAR(500) NULL,
    ADD COLUMN enriched_at  DATETIME     NULL;
