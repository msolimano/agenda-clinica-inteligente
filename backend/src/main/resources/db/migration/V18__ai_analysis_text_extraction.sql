ALTER TABLE ai_analyses
    ADD COLUMN IF NOT EXISTS extracted_text text,
    ADD COLUMN IF NOT EXISTS extracted_text_preview text,
    ADD COLUMN IF NOT EXISTS text_extraction_status varchar(40) DEFAULT 'not_requested',
    ADD COLUMN IF NOT EXISTS text_extraction_method varchar(80),
    ADD COLUMN IF NOT EXISTS text_extraction_confidence numeric(5,4),
    ADD COLUMN IF NOT EXISTS text_extraction_error_message text;

CREATE INDEX IF NOT EXISTS ix_ai_analyses_text_extraction_status
    ON ai_analyses (text_extraction_status, created_at DESC)
    WHERE status <> 'deleted';
