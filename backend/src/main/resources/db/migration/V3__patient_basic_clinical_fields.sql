ALTER TABLE patients
    ADD COLUMN emergency_contact_name varchar(160),
    ADD COLUMN emergency_contact_phone varchar(40),
    ADD COLUMN emergency_contact_relationship varchar(80);
