CREATE TABLE clinical_evolutions (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id uuid NOT NULL REFERENCES organizations(id),
    clinical_record_id uuid NOT NULL REFERENCES clinical_records(id),
    patient_id uuid NOT NULL REFERENCES patients(id),
    professional_id uuid NOT NULL REFERENCES professionals(id),
    evolution_date timestamptz NOT NULL DEFAULT now(),
    subjective text,
    objective text,
    assessment text,
    plan text,
    notes text,
    evolution_status varchar(40) NOT NULL DEFAULT 'draft',
    status varchar(30) NOT NULL DEFAULT 'active',
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT chk_clinical_evolutions_evolution_status CHECK (evolution_status IN ('draft', 'active', 'corrected', 'cancelled')),
    CONSTRAINT chk_clinical_evolutions_status CHECK (status IN ('active', 'deleted'))
);

CREATE INDEX ix_clinical_evolutions_record
    ON clinical_evolutions (organization_id, clinical_record_id, evolution_date DESC, created_at DESC)
    WHERE status = 'active';

CREATE INDEX ix_clinical_evolutions_patient
    ON clinical_evolutions (organization_id, patient_id, evolution_date DESC, created_at DESC)
    WHERE status = 'active';

CREATE INDEX ix_clinical_evolutions_professional
    ON clinical_evolutions (organization_id, professional_id, evolution_date DESC, created_at DESC)
    WHERE status = 'active';

CREATE TRIGGER trg_clinical_evolutions_updated_at
    BEFORE UPDATE ON clinical_evolutions
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
