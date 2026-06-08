ALTER TABLE waiting_list
    ADD COLUMN scheduled_appointment_id uuid REFERENCES appointments(id),
    ADD COLUMN contacted_at timestamptz;

CREATE INDEX ix_waiting_list_filters
    ON waiting_list (organization_id, status, specialty_id, professional_id);

CREATE INDEX ix_waiting_list_scheduled_appointment
    ON waiting_list (scheduled_appointment_id)
    WHERE scheduled_appointment_id IS NOT NULL;

CREATE INDEX ix_waiting_list_active_priority
    ON waiting_list (organization_id, specialty_id, professional_id, priority, created_at)
    WHERE status IN ('waiting', 'contacted');
