ALTER TABLE clinical_documents
    ADD COLUMN IF NOT EXISTS professional_id uuid REFERENCES professionals(id),
    ADD COLUMN IF NOT EXISTS title varchar(180),
    ADD COLUMN IF NOT EXISTS description text,
    ADD COLUMN IF NOT EXISTS checksum_sha256 varchar(128),
    ADD COLUMN IF NOT EXISTS ai_analysis_status varchar(40) NOT NULL DEFAULT 'not_requested';

ALTER TABLE clinical_documents
    ALTER COLUMN checksum_sha256 TYPE varchar(128),
    ALTER COLUMN ai_analysis_status TYPE varchar(40),
    ALTER COLUMN ai_analysis_status SET DEFAULT 'not_requested';

CREATE INDEX IF NOT EXISTS ix_clinical_documents_professional_created
    ON clinical_documents (organization_id, professional_id, created_at DESC)
    WHERE professional_id IS NOT NULL AND status <> 'deleted';

CREATE INDEX IF NOT EXISTS ix_clinical_documents_type_created
    ON clinical_documents (organization_id, document_type, created_at DESC)
    WHERE status <> 'deleted';
