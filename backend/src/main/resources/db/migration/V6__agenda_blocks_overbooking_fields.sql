ALTER TABLE appointments
    ADD COLUMN is_overbooking boolean NOT NULL DEFAULT false;

ALTER TABLE professional_availability
    ADD COLUMN allows_overbooking boolean NOT NULL DEFAULT false,
    ADD COLUMN max_overbookings integer NOT NULL DEFAULT 0;

ALTER TABLE professional_availability
    ADD CONSTRAINT chk_professional_availability_max_overbookings CHECK (max_overbookings >= 0);

CREATE INDEX ix_appointments_professional_range_status
    ON appointments (professional_id, start_at, end_at, status);

CREATE INDEX ix_appointments_blocked_slots_active
    ON appointments (professional_id, start_at, end_at)
    WHERE appointment_type = 'blocked_slot' AND status = 'blocked';

CREATE INDEX ix_appointments_overbooking_active
    ON appointments (professional_id, start_at, end_at)
    WHERE is_overbooking = true AND status IN ('scheduled', 'confirmed');

CREATE INDEX ix_overbookings_professional_status
    ON overbookings (professional_id, status, created_at);
