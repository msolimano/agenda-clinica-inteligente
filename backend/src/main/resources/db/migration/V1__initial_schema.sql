CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS trigger AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TABLE organizations (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    name varchar(160) NOT NULL,
    legal_name varchar(200),
    identifier varchar(80),
    status varchar(30) NOT NULL DEFAULT 'active',
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT chk_organizations_status CHECK (status IN ('active', 'inactive', 'deleted'))
);

CREATE UNIQUE INDEX ux_organizations_identifier_active
    ON organizations (identifier)
    WHERE identifier IS NOT NULL AND status <> 'deleted';

CREATE TABLE users (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id uuid NOT NULL REFERENCES organizations(id),
    email varchar(255) NOT NULL,
    password_hash varchar(255) NOT NULL,
    first_name varchar(120) NOT NULL,
    last_name varchar(120) NOT NULL,
    role varchar(40) NOT NULL,
    status varchar(30) NOT NULL DEFAULT 'active',
    last_login_at timestamptz,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT chk_users_role CHECK (role IN ('administrator', 'professional', 'patient')),
    CONSTRAINT chk_users_status CHECK (status IN ('active', 'inactive', 'locked', 'deleted'))
);

CREATE UNIQUE INDEX ux_users_organization_email_active
    ON users (organization_id, lower(email))
    WHERE status <> 'deleted';

CREATE TABLE specialties (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id uuid REFERENCES organizations(id),
    name varchar(140) NOT NULL,
    code varchar(60),
    status varchar(30) NOT NULL DEFAULT 'active',
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT chk_specialties_status CHECK (status IN ('active', 'inactive', 'deleted'))
);

CREATE UNIQUE INDEX ux_specialties_organization_name_active
    ON specialties (organization_id, lower(name))
    WHERE status <> 'deleted';

CREATE TABLE professionals (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id uuid NOT NULL REFERENCES organizations(id),
    user_id uuid REFERENCES users(id),
    document_type varchar(40),
    document_number varchar(80),
    registry_number varchar(80),
    first_name varchar(120) NOT NULL,
    last_name varchar(120) NOT NULL,
    email varchar(255),
    phone varchar(40),
    status varchar(30) NOT NULL DEFAULT 'active',
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT chk_professionals_status CHECK (status IN ('active', 'inactive', 'deleted'))
);

CREATE UNIQUE INDEX ux_professionals_user_active
    ON professionals (user_id)
    WHERE user_id IS NOT NULL AND status <> 'deleted';

CREATE UNIQUE INDEX ux_professionals_document_active
    ON professionals (organization_id, document_type, document_number)
    WHERE document_type IS NOT NULL AND document_number IS NOT NULL AND status <> 'deleted';

CREATE UNIQUE INDEX ux_professionals_registry_active
    ON professionals (organization_id, registry_number)
    WHERE registry_number IS NOT NULL AND status <> 'deleted';

CREATE TABLE professional_specialties (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    professional_id uuid NOT NULL REFERENCES professionals(id),
    specialty_id uuid NOT NULL REFERENCES specialties(id),
    status varchar(30) NOT NULL DEFAULT 'active',
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT chk_professional_specialties_status CHECK (status IN ('active', 'inactive', 'deleted'))
);

CREATE UNIQUE INDEX ux_professional_specialties_active
    ON professional_specialties (professional_id, specialty_id)
    WHERE status <> 'deleted';

CREATE TABLE patients (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id uuid NOT NULL REFERENCES organizations(id),
    user_id uuid REFERENCES users(id),
    document_type varchar(40),
    document_number varchar(80),
    first_name varchar(120) NOT NULL,
    last_name varchar(120) NOT NULL,
    birth_date date,
    sex varchar(30),
    email varchar(255),
    phone varchar(40),
    address varchar(300),
    status varchar(30) NOT NULL DEFAULT 'active',
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT chk_patients_sex CHECK (sex IS NULL OR sex IN ('female', 'male', 'other', 'unknown')),
    CONSTRAINT chk_patients_status CHECK (status IN ('active', 'inactive', 'deleted'))
);

CREATE UNIQUE INDEX ux_patients_user_active
    ON patients (user_id)
    WHERE user_id IS NOT NULL AND status <> 'deleted';

CREATE UNIQUE INDEX ux_patients_document_active
    ON patients (organization_id, document_type, document_number)
    WHERE document_type IS NOT NULL AND document_number IS NOT NULL AND status <> 'deleted';

CREATE TABLE professional_availability (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id uuid NOT NULL REFERENCES organizations(id),
    professional_id uuid NOT NULL REFERENCES professionals(id),
    weekday smallint NOT NULL,
    start_time time NOT NULL,
    end_time time NOT NULL,
    slot_minutes integer NOT NULL DEFAULT 30,
    location varchar(180),
    valid_from date,
    valid_to date,
    status varchar(30) NOT NULL DEFAULT 'active',
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT chk_professional_availability_weekday CHECK (weekday BETWEEN 1 AND 7),
    CONSTRAINT chk_professional_availability_time CHECK (start_time < end_time),
    CONSTRAINT chk_professional_availability_slot CHECK (slot_minutes > 0),
    CONSTRAINT chk_professional_availability_dates CHECK (valid_to IS NULL OR valid_from IS NULL OR valid_from <= valid_to),
    CONSTRAINT chk_professional_availability_status CHECK (status IN ('active', 'inactive', 'deleted'))
);

CREATE TABLE appointments (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id uuid NOT NULL REFERENCES organizations(id),
    professional_id uuid NOT NULL REFERENCES professionals(id),
    patient_id uuid REFERENCES patients(id),
    start_at timestamptz NOT NULL,
    end_at timestamptz NOT NULL,
    appointment_type varchar(40) NOT NULL DEFAULT 'consultation',
    status varchar(30) NOT NULL DEFAULT 'scheduled',
    reason varchar(300),
    cancellation_reason varchar(300),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT chk_appointments_time CHECK (start_at < end_at),
    CONSTRAINT chk_appointments_type CHECK (appointment_type IN ('consultation', 'control', 'procedure', 'blocked_slot')),
    CONSTRAINT chk_appointments_status CHECK (status IN ('scheduled', 'confirmed', 'completed', 'cancelled', 'no_show', 'blocked', 'deleted'))
);

CREATE INDEX ix_appointments_professional_start
    ON appointments (organization_id, professional_id, start_at);

CREATE INDEX ix_appointments_patient_start
    ON appointments (organization_id, patient_id, start_at)
    WHERE patient_id IS NOT NULL;

CREATE TABLE waiting_list (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id uuid NOT NULL REFERENCES organizations(id),
    patient_id uuid NOT NULL REFERENCES patients(id),
    professional_id uuid REFERENCES professionals(id),
    specialty_id uuid REFERENCES specialties(id),
    requested_from date,
    requested_to date,
    priority integer NOT NULL DEFAULT 3,
    availability_notes text,
    status varchar(30) NOT NULL DEFAULT 'waiting',
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT chk_waiting_list_dates CHECK (requested_to IS NULL OR requested_from IS NULL OR requested_from <= requested_to),
    CONSTRAINT chk_waiting_list_priority CHECK (priority BETWEEN 1 AND 5),
    CONSTRAINT chk_waiting_list_status CHECK (status IN ('waiting', 'contacted', 'scheduled', 'cancelled', 'deleted'))
);

CREATE TABLE overbookings (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id uuid NOT NULL REFERENCES organizations(id),
    appointment_id uuid NOT NULL REFERENCES appointments(id),
    professional_id uuid NOT NULL REFERENCES professionals(id),
    patient_id uuid NOT NULL REFERENCES patients(id),
    approved_by_user_id uuid REFERENCES users(id),
    reason varchar(300) NOT NULL,
    status varchar(30) NOT NULL DEFAULT 'requested',
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT chk_overbookings_status CHECK (status IN ('requested', 'approved', 'rejected', 'cancelled', 'deleted'))
);

CREATE UNIQUE INDEX ux_overbookings_appointment_active
    ON overbookings (appointment_id)
    WHERE status <> 'deleted';

CREATE TABLE clinical_records (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id uuid NOT NULL REFERENCES organizations(id),
    patient_id uuid NOT NULL REFERENCES patients(id),
    professional_id uuid NOT NULL REFERENCES professionals(id),
    appointment_id uuid REFERENCES appointments(id),
    status varchar(30) NOT NULL DEFAULT 'draft',
    finalized_at timestamptz,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT chk_clinical_records_status CHECK (status IN ('draft', 'finalized', 'cancelled', 'deleted'))
);

CREATE UNIQUE INDEX ux_clinical_records_appointment_active
    ON clinical_records (appointment_id)
    WHERE appointment_id IS NOT NULL AND status <> 'deleted';

CREATE INDEX ix_clinical_records_patient_created
    ON clinical_records (organization_id, patient_id, created_at DESC);

CREATE TABLE clinical_notes (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id uuid NOT NULL REFERENCES organizations(id),
    clinical_record_id uuid NOT NULL REFERENCES clinical_records(id),
    note_type varchar(60) NOT NULL,
    content text NOT NULL,
    status varchar(30) NOT NULL DEFAULT 'active',
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT chk_clinical_notes_type CHECK (
        note_type IN (
            'consultation_reason',
            'anamnesis',
            'clinical_impression',
            'plan',
            'patient_indications',
            'exam_request',
            'image_request',
            'general'
        )
    ),
    CONSTRAINT chk_clinical_notes_status CHECK (status IN ('active', 'deleted'))
);

CREATE TABLE clinical_documents (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id uuid NOT NULL REFERENCES organizations(id),
    patient_id uuid NOT NULL REFERENCES patients(id),
    clinical_record_id uuid REFERENCES clinical_records(id),
    document_type varchar(60) NOT NULL,
    original_filename varchar(255) NOT NULL,
    mime_type varchar(120) NOT NULL,
    file_size_bytes bigint,
    storage_provider varchar(30) NOT NULL DEFAULT 'local',
    bucket_name varchar(160),
    object_key varchar(500) NOT NULL,
    storage_url text,
    checksum_sha256 varchar(64),
    ai_analysis_status varchar(30) NOT NULL DEFAULT 'not_requested',
    status varchar(30) NOT NULL DEFAULT 'active',
    uploaded_by_user_id uuid REFERENCES users(id),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT chk_clinical_documents_size CHECK (file_size_bytes IS NULL OR file_size_bytes >= 0),
    CONSTRAINT chk_clinical_documents_storage_provider CHECK (storage_provider IN ('local', 's3')),
    CONSTRAINT chk_clinical_documents_ai_status CHECK (ai_analysis_status IN ('not_requested', 'pending', 'processing', 'completed', 'failed')),
    CONSTRAINT chk_clinical_documents_status CHECK (status IN ('active', 'inactive', 'deleted'))
);

CREATE INDEX ix_clinical_documents_patient_created
    ON clinical_documents (organization_id, patient_id, created_at DESC)
    WHERE status <> 'deleted';

CREATE TABLE clinical_images (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id uuid NOT NULL REFERENCES organizations(id),
    patient_id uuid NOT NULL REFERENCES patients(id),
    clinical_record_id uuid REFERENCES clinical_records(id),
    report_document_id uuid REFERENCES clinical_documents(id),
    image_type varchar(60) NOT NULL,
    original_filename varchar(255) NOT NULL,
    mime_type varchar(120) NOT NULL,
    file_size_bytes bigint,
    storage_provider varchar(30) NOT NULL DEFAULT 'local',
    bucket_name varchar(160),
    object_key varchar(500) NOT NULL,
    thumbnail_object_key varchar(500),
    image_url text,
    thumbnail_url text,
    ai_analysis_status varchar(30) NOT NULL DEFAULT 'not_requested',
    status varchar(30) NOT NULL DEFAULT 'active',
    uploaded_by_user_id uuid REFERENCES users(id),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT chk_clinical_images_type CHECK (image_type IN ('dermatology_photo', 'ultrasound', 'xray', 'other')),
    CONSTRAINT chk_clinical_images_size CHECK (file_size_bytes IS NULL OR file_size_bytes >= 0),
    CONSTRAINT chk_clinical_images_storage_provider CHECK (storage_provider IN ('local', 's3')),
    CONSTRAINT chk_clinical_images_ai_status CHECK (ai_analysis_status IN ('not_requested', 'pending', 'processing', 'completed', 'failed')),
    CONSTRAINT chk_clinical_images_status CHECK (status IN ('active', 'inactive', 'deleted'))
);

CREATE INDEX ix_clinical_images_patient_created
    ON clinical_images (organization_id, patient_id, created_at DESC)
    WHERE status <> 'deleted';

ALTER TABLE clinical_documents
    ADD COLUMN related_clinical_image_id uuid REFERENCES clinical_images(id);

CREATE TABLE consents (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id uuid NOT NULL REFERENCES organizations(id),
    patient_id uuid NOT NULL REFERENCES patients(id),
    consent_type varchar(60) NOT NULL,
    granted boolean NOT NULL DEFAULT false,
    granted_at timestamptz,
    revoked_at timestamptz,
    source varchar(80),
    notes text,
    status varchar(30) NOT NULL DEFAULT 'active',
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT chk_consents_type CHECK (consent_type IN ('ai_analysis', 'document_access', 'image_access')),
    CONSTRAINT chk_consents_dates CHECK (revoked_at IS NULL OR granted_at IS NULL OR granted_at <= revoked_at),
    CONSTRAINT chk_consents_status CHECK (status IN ('active', 'revoked', 'deleted'))
);

CREATE INDEX ix_consents_patient_type
    ON consents (organization_id, patient_id, consent_type, created_at DESC)
    WHERE status <> 'deleted';

CREATE TABLE ai_analyses (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id uuid NOT NULL REFERENCES organizations(id),
    patient_id uuid NOT NULL REFERENCES patients(id),
    clinical_record_id uuid REFERENCES clinical_records(id),
    clinical_document_id uuid REFERENCES clinical_documents(id),
    clinical_image_id uuid REFERENCES clinical_images(id),
    requested_by_user_id uuid REFERENCES users(id),
    reviewed_by_user_id uuid REFERENCES users(id),
    analysis_type varchar(60) NOT NULL,
    status varchar(30) NOT NULL DEFAULT 'pending',
    model_name varchar(120),
    source_summary text,
    result_summary text,
    relevant_findings jsonb,
    disclaimer_acknowledged boolean NOT NULL DEFAULT false,
    reviewed_at timestamptz,
    error_message text,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT chk_ai_analyses_type CHECK (analysis_type IN ('clinical_document_summary', 'clinical_image_summary', 'patient_background_summary')),
    CONSTRAINT chk_ai_analyses_status CHECK (status IN ('pending', 'processing', 'completed', 'failed', 'reviewed', 'rejected', 'deleted')),
    CONSTRAINT chk_ai_analyses_source CHECK (
        clinical_record_id IS NOT NULL OR clinical_document_id IS NOT NULL OR clinical_image_id IS NOT NULL
    )
);

CREATE INDEX ix_ai_analyses_patient_created
    ON ai_analyses (organization_id, patient_id, created_at DESC)
    WHERE status <> 'deleted';

CREATE TABLE ai_recommendations (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id uuid NOT NULL REFERENCES organizations(id),
    professional_id uuid REFERENCES professionals(id),
    patient_id uuid REFERENCES patients(id),
    appointment_id uuid REFERENCES appointments(id),
    waiting_list_id uuid REFERENCES waiting_list(id),
    recommendation_type varchar(60) NOT NULL,
    recommendation_window varchar(60),
    status varchar(30) NOT NULL DEFAULT 'pending',
    explanation text NOT NULL,
    score numeric(5,2),
    metadata jsonb,
    generated_by_model varchar(120),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT chk_ai_recommendations_type CHECK (recommendation_type IN ('released_slot', 'compatible_patient', 'overbooking', 'idle_block')),
    CONSTRAINT chk_ai_recommendations_status CHECK (status IN ('pending', 'accepted', 'rejected', 'expired', 'deleted')),
    CONSTRAINT chk_ai_recommendations_score CHECK (score IS NULL OR (score >= 0 AND score <= 100))
);

CREATE INDEX ix_ai_recommendations_professional_created
    ON ai_recommendations (organization_id, professional_id, created_at DESC)
    WHERE professional_id IS NOT NULL AND status <> 'deleted';

CREATE TABLE audit_events (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id uuid REFERENCES organizations(id),
    actor_user_id uuid REFERENCES users(id),
    entity_type varchar(120) NOT NULL,
    entity_id uuid,
    action varchar(120) NOT NULL,
    event_data jsonb,
    ip_address inet,
    user_agent text,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX ix_audit_events_entity
    ON audit_events (entity_type, entity_id, created_at DESC);

CREATE INDEX ix_audit_events_actor_created
    ON audit_events (actor_user_id, created_at DESC)
    WHERE actor_user_id IS NOT NULL;

CREATE TRIGGER trg_organizations_updated_at
    BEFORE UPDATE ON organizations
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_specialties_updated_at
    BEFORE UPDATE ON specialties
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_professionals_updated_at
    BEFORE UPDATE ON professionals
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_professional_specialties_updated_at
    BEFORE UPDATE ON professional_specialties
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_patients_updated_at
    BEFORE UPDATE ON patients
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_professional_availability_updated_at
    BEFORE UPDATE ON professional_availability
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_appointments_updated_at
    BEFORE UPDATE ON appointments
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_waiting_list_updated_at
    BEFORE UPDATE ON waiting_list
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_overbookings_updated_at
    BEFORE UPDATE ON overbookings
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_clinical_records_updated_at
    BEFORE UPDATE ON clinical_records
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_clinical_notes_updated_at
    BEFORE UPDATE ON clinical_notes
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_clinical_documents_updated_at
    BEFORE UPDATE ON clinical_documents
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_clinical_images_updated_at
    BEFORE UPDATE ON clinical_images
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_consents_updated_at
    BEFORE UPDATE ON consents
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_ai_analyses_updated_at
    BEFORE UPDATE ON ai_analyses
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_ai_recommendations_updated_at
    BEFORE UPDATE ON ai_recommendations
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
