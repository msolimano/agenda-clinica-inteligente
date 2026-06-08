ALTER TABLE clinical_records
    ADD COLUMN IF NOT EXISTS record_date timestamptz,
    ADD COLUMN IF NOT EXISTS chief_complaint text,
    ADD COLUMN IF NOT EXISTS anamnesis text,
    ADD COLUMN IF NOT EXISTS physical_exam text,
    ADD COLUMN IF NOT EXISTS assessment text,
    ADD COLUMN IF NOT EXISTS plan text,
    ADD COLUMN IF NOT EXISTS notes text;

UPDATE clinical_records
SET record_date = COALESCE(record_date, created_at, now())
WHERE record_date IS NULL;

UPDATE clinical_records
SET status = 'closed'
WHERE status IN ('finalized', 'cancelled');

ALTER TABLE clinical_records
    ALTER COLUMN record_date SET DEFAULT now(),
    ALTER COLUMN record_date SET NOT NULL;

ALTER TABLE clinical_records
    DROP CONSTRAINT IF EXISTS chk_clinical_records_status;

ALTER TABLE clinical_records
    ADD CONSTRAINT chk_clinical_records_status CHECK (status IN ('draft', 'open', 'closed', 'deleted'));

CREATE INDEX IF NOT EXISTS ix_clinical_records_patient_record_date
    ON clinical_records (organization_id, patient_id, record_date DESC)
    WHERE status <> 'deleted';

CREATE INDEX IF NOT EXISTS ix_clinical_records_professional_record_date
    ON clinical_records (organization_id, professional_id, record_date DESC)
    WHERE status <> 'deleted';
