ALTER TABLE ai_analyses
    ADD COLUMN IF NOT EXISTS provider_name varchar(60),
    ADD COLUMN IF NOT EXISTS provider_request_id varchar(180),
    ADD COLUMN IF NOT EXISTS prompt_version varchar(60),
    ADD COLUMN IF NOT EXISTS input_token_count integer,
    ADD COLUMN IF NOT EXISTS output_token_count integer,
    ADD COLUMN IF NOT EXISTS total_token_count integer,
    ADD COLUMN IF NOT EXISTS latency_ms integer,
    ADD COLUMN IF NOT EXISTS provider_error_code varchar(120),
    ADD COLUMN IF NOT EXISTS provider_metadata jsonb NOT NULL DEFAULT '{}'::jsonb;

CREATE INDEX IF NOT EXISTS ix_ai_analyses_provider_created
    ON ai_analyses (provider_name, created_at DESC)
    WHERE status <> 'deleted';
