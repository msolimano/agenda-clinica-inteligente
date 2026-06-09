CREATE TABLE medication_catalog (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id uuid REFERENCES organizations(id),
    medication_code varchar(80),
    medication_code_system varchar(80),
    medication_name varchar(255) NOT NULL,
    active_ingredient varchar(255),
    presentation varchar(180),
    strength varchar(120),
    pharmaceutical_form varchar(120),
    route varchar(120),
    manufacturer varchar(180),
    status varchar(30) NOT NULL DEFAULT 'active',
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT chk_medication_catalog_status CHECK (status IN ('active', 'inactive', 'deleted'))
);

ALTER TABLE clinical_prescriptions
    ADD CONSTRAINT fk_clinical_prescriptions_medication_catalog
    FOREIGN KEY (medication_catalog_id) REFERENCES medication_catalog(id);

CREATE INDEX ix_medication_catalog_name
    ON medication_catalog (organization_id, lower(medication_name))
    WHERE status <> 'deleted';

CREATE INDEX ix_medication_catalog_active_ingredient
    ON medication_catalog (organization_id, lower(active_ingredient))
    WHERE status <> 'deleted' AND active_ingredient IS NOT NULL;

CREATE INDEX ix_medication_catalog_code
    ON medication_catalog (organization_id, lower(medication_code))
    WHERE status <> 'deleted' AND medication_code IS NOT NULL;

CREATE INDEX ix_medication_catalog_status
    ON medication_catalog (organization_id, status, medication_name);

CREATE INDEX ix_clinical_prescriptions_medication_catalog
    ON clinical_prescriptions (organization_id, medication_catalog_id)
    WHERE status = 'active' AND medication_catalog_id IS NOT NULL;

CREATE TRIGGER trg_medication_catalog_updated_at
    BEFORE UPDATE ON medication_catalog
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
