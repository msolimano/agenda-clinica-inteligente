CREATE TABLE clinical_diagnoses (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id uuid NOT NULL REFERENCES organizations(id),
    clinical_record_id uuid NOT NULL REFERENCES clinical_records(id),
    patient_id uuid NOT NULL REFERENCES patients(id),
    professional_id uuid NOT NULL REFERENCES professionals(id),
    diagnosis_text text NOT NULL,
    is_primary boolean NOT NULL DEFAULT false,
    diagnosis_status varchar(40) NOT NULL DEFAULT 'suspected',
    observations text,
    code_system varchar(80),
    diagnosis_code varchar(80),
    diagnosis_code_display varchar(255),
    status varchar(30) NOT NULL DEFAULT 'active',
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT chk_clinical_diagnoses_status CHECK (status IN ('active', 'deleted')),
    CONSTRAINT chk_clinical_diagnoses_diagnosis_status CHECK (diagnosis_status IN ('suspected', 'confirmed', 'resolved', 'ruled_out'))
);

CREATE INDEX ix_clinical_diagnoses_record
    ON clinical_diagnoses (organization_id, clinical_record_id, is_primary DESC, created_at DESC)
    WHERE status = 'active';

CREATE INDEX ix_clinical_diagnoses_patient
    ON clinical_diagnoses (organization_id, patient_id, created_at DESC)
    WHERE status = 'active';

CREATE INDEX ix_clinical_diagnoses_professional
    ON clinical_diagnoses (organization_id, professional_id, created_at DESC)
    WHERE status = 'active';

CREATE UNIQUE INDEX ux_clinical_diagnoses_primary_active
    ON clinical_diagnoses (clinical_record_id)
    WHERE is_primary IS TRUE AND status = 'active';

CREATE TRIGGER trg_clinical_diagnoses_updated_at
    BEFORE UPDATE ON clinical_diagnoses
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
