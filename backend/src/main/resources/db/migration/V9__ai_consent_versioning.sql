ALTER TABLE consents
    ADD COLUMN IF NOT EXISTS consent_version varchar(40);

UPDATE consents
SET consent_version = 'IA-CONSENT-V1'
WHERE consent_type = 'ai_analysis'
  AND consent_version IS NULL;

ALTER TABLE ai_analyses
    ADD COLUMN IF NOT EXISTS ai_consent_id uuid REFERENCES consents(id);

CREATE UNIQUE INDEX IF NOT EXISTS ux_consents_active_ai_per_patient
    ON consents (organization_id, patient_id, consent_type)
    WHERE consent_type = 'ai_analysis'
      AND granted = true
      AND status = 'active';

CREATE INDEX IF NOT EXISTS ix_consents_ai_patient_status
    ON consents (organization_id, patient_id, status, created_at DESC)
    WHERE consent_type = 'ai_analysis'
      AND status <> 'deleted';

CREATE INDEX IF NOT EXISTS ix_ai_analyses_ai_consent
    ON ai_analyses (ai_consent_id)
    WHERE ai_consent_id IS NOT NULL;
