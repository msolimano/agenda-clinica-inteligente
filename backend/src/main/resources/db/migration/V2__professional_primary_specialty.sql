ALTER TABLE professional_specialties
    ADD COLUMN is_primary boolean NOT NULL DEFAULT false;

CREATE UNIQUE INDEX ux_professional_specialties_primary_active
    ON professional_specialties (professional_id)
    WHERE is_primary = true AND status = 'active';
