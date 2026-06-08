ALTER TABLE appointments
    ADD COLUMN rescheduled_from_id uuid REFERENCES appointments(id),
    ADD COLUMN confirmed_at timestamptz,
    ADD COLUMN cancelled_at timestamptz,
    ADD COLUMN no_show_at timestamptz;

CREATE INDEX ix_appointments_rescheduled_from
    ON appointments (rescheduled_from_id)
    WHERE rescheduled_from_id IS NOT NULL;

CREATE INDEX ix_professional_availability_professional_weekday
    ON professional_availability (professional_id, weekday)
    WHERE status = 'active';
