CREATE TABLE diagnosis_catalog (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id uuid REFERENCES organizations(id),
    diagnosis_code varchar(80),
    code_system varchar(80),
    diagnosis_display varchar(255) NOT NULL,
    category varchar(160),
    description text,
    status varchar(30) NOT NULL DEFAULT 'active',
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT chk_diagnosis_catalog_status CHECK (status IN ('active', 'inactive', 'deleted'))
);

ALTER TABLE clinical_diagnoses
    ADD COLUMN diagnosis_catalog_id uuid;

ALTER TABLE clinical_diagnoses
    ADD CONSTRAINT fk_clinical_diagnoses_diagnosis_catalog
    FOREIGN KEY (diagnosis_catalog_id) REFERENCES diagnosis_catalog(id);

CREATE INDEX ix_diagnosis_catalog_display
    ON diagnosis_catalog (organization_id, lower(diagnosis_display))
    WHERE status <> 'deleted';

CREATE INDEX ix_diagnosis_catalog_code
    ON diagnosis_catalog (organization_id, lower(diagnosis_code))
    WHERE status <> 'deleted' AND diagnosis_code IS NOT NULL;

CREATE INDEX ix_diagnosis_catalog_category
    ON diagnosis_catalog (organization_id, lower(category))
    WHERE status <> 'deleted' AND category IS NOT NULL;

CREATE INDEX ix_diagnosis_catalog_status
    ON diagnosis_catalog (organization_id, status, diagnosis_display);

CREATE INDEX ix_clinical_diagnoses_diagnosis_catalog
    ON clinical_diagnoses (organization_id, diagnosis_catalog_id)
    WHERE status = 'active' AND diagnosis_catalog_id IS NOT NULL;

CREATE TRIGGER trg_diagnosis_catalog_updated_at
    BEFORE UPDATE ON diagnosis_catalog
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
