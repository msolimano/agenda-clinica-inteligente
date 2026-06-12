CREATE TABLE IF NOT EXISTS clinical_insights (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id uuid NOT NULL REFERENCES organizations(id),
    patient_id uuid NOT NULL REFERENCES patients(id),
    clinical_document_id uuid NOT NULL REFERENCES clinical_documents(id),
    clinical_record_id uuid REFERENCES clinical_records(id),
    professional_id uuid REFERENCES professionals(id),
    ai_analysis_id uuid REFERENCES ai_analyses(id),
    source_document_name varchar(255) NOT NULL,
    insight_type varchar(60) NOT NULL,
    title varchar(180) NOT NULL,
    description text NOT NULL,
    source_text text,
    confidence numeric(5,4),
    status varchar(30) NOT NULL DEFAULT 'pending',
    reviewed_at timestamptz,
    reviewed_by_professional_id uuid REFERENCES professionals(id),
    review_notes text,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT chk_clinical_insights_type CHECK (insight_type IN (
        'clinical_summary',
        'clinical_alert',
        'diagnosis_candidate',
        'medication_candidate',
        'allergy_candidate',
        'risk_factor_candidate',
        'lab_result_candidate',
        'observation_candidate'
    )),
    CONSTRAINT chk_clinical_insights_status CHECK (status IN ('pending', 'accepted', 'rejected', 'dismissed')),
    CONSTRAINT chk_clinical_insights_confidence CHECK (confidence IS NULL OR (confidence >= 0 AND confidence <= 1))
);

CREATE INDEX IF NOT EXISTS ix_clinical_insights_patient_created
    ON clinical_insights (organization_id, patient_id, created_at DESC);

CREATE INDEX IF NOT EXISTS ix_clinical_insights_document
    ON clinical_insights (organization_id, clinical_document_id);

CREATE INDEX IF NOT EXISTS ix_clinical_insights_status
    ON clinical_insights (organization_id, status);

CREATE INDEX IF NOT EXISTS ix_clinical_insights_type
    ON clinical_insights (organization_id, insight_type);

CREATE INDEX IF NOT EXISTS ix_clinical_insights_ai_analysis
    ON clinical_insights (ai_analysis_id)
    WHERE ai_analysis_id IS NOT NULL;

DROP TRIGGER IF EXISTS trg_clinical_insights_updated_at ON clinical_insights;
CREATE TRIGGER trg_clinical_insights_updated_at
    BEFORE UPDATE ON clinical_insights
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
