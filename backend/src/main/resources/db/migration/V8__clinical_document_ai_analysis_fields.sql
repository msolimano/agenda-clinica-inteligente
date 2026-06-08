ALTER TABLE ai_analyses
    ADD COLUMN IF NOT EXISTS clinical_summary text,
    ADD COLUMN IF NOT EXISTS mentioned_diagnoses jsonb,
    ADD COLUMN IF NOT EXISTS mentioned_medications jsonb,
    ADD COLUMN IF NOT EXISTS mentioned_allergies jsonb,
    ADD COLUMN IF NOT EXISTS recommendations text,
    ADD COLUMN IF NOT EXISTS started_at timestamptz,
    ADD COLUMN IF NOT EXISTS completed_at timestamptz;

CREATE INDEX IF NOT EXISTS ix_ai_analyses_document_created
    ON ai_analyses (clinical_document_id, created_at DESC)
    WHERE clinical_document_id IS NOT NULL AND status <> 'deleted';

CREATE INDEX IF NOT EXISTS ix_ai_analyses_status_created
    ON ai_analyses (status, created_at DESC)
    WHERE status <> 'deleted';

CREATE UNIQUE INDEX IF NOT EXISTS ux_ai_analyses_document_in_progress
    ON ai_analyses (clinical_document_id)
    WHERE clinical_document_id IS NOT NULL AND status IN ('pending', 'processing');
