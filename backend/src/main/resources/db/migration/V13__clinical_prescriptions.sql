CREATE TABLE clinical_prescriptions (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id uuid NOT NULL REFERENCES organizations(id),
    clinical_record_id uuid NOT NULL REFERENCES clinical_records(id),
    patient_id uuid NOT NULL REFERENCES patients(id),
    professional_id uuid NOT NULL REFERENCES professionals(id),
    diagnosis_id uuid REFERENCES clinical_diagnoses(id),
    medication_name text NOT NULL,
    dosage text NOT NULL,
    frequency text NOT NULL,
    duration text,
    route text,
    patient_instructions text,
    clinical_notes text,
    prescription_status varchar(40) NOT NULL DEFAULT 'draft',
    status varchar(30) NOT NULL DEFAULT 'active',
    medication_catalog_id uuid,
    medication_code varchar(80),
    medication_code_system varchar(80),
    medication_code_display varchar(255),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT chk_clinical_prescriptions_prescription_status CHECK (prescription_status IN ('draft', 'active', 'suspended', 'completed', 'cancelled')),
    CONSTRAINT chk_clinical_prescriptions_status CHECK (status IN ('active', 'deleted'))
);

CREATE INDEX ix_clinical_prescriptions_record
    ON clinical_prescriptions (organization_id, clinical_record_id, created_at DESC)
    WHERE status = 'active';

CREATE INDEX ix_clinical_prescriptions_patient
    ON clinical_prescriptions (organization_id, patient_id, created_at DESC)
    WHERE status = 'active';

CREATE INDEX ix_clinical_prescriptions_professional
    ON clinical_prescriptions (organization_id, professional_id, created_at DESC)
    WHERE status = 'active';

CREATE INDEX ix_clinical_prescriptions_diagnosis
    ON clinical_prescriptions (organization_id, diagnosis_id)
    WHERE status = 'active' AND diagnosis_id IS NOT NULL;

CREATE TRIGGER trg_clinical_prescriptions_updated_at
    BEFORE UPDATE ON clinical_prescriptions
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
