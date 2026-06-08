package com.iclinical.technology.appointments.waitinglist;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "waiting_list")
public class WaitingListEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Column(name = "professional_id")
    private UUID professionalId;

    @Column(name = "specialty_id")
    private UUID specialtyId;

    @Column(name = "requested_from")
    private LocalDate requestedFrom;

    @Column(name = "requested_to")
    private LocalDate requestedTo;

    @Column(nullable = false)
    private int priority = 3;

    @Column(name = "availability_notes")
    private String availabilityNotes;

    @Column(nullable = false, length = 30)
    private String status = "waiting";

    @Column(name = "scheduled_appointment_id")
    private UUID scheduledAppointmentId;

    @Column(name = "contacted_at")
    private Instant contactedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        var now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getOrganizationId() { return organizationId; }
    public void setOrganizationId(UUID organizationId) { this.organizationId = organizationId; }
    public UUID getPatientId() { return patientId; }
    public void setPatientId(UUID patientId) { this.patientId = patientId; }
    public UUID getProfessionalId() { return professionalId; }
    public void setProfessionalId(UUID professionalId) { this.professionalId = professionalId; }
    public UUID getSpecialtyId() { return specialtyId; }
    public void setSpecialtyId(UUID specialtyId) { this.specialtyId = specialtyId; }
    public LocalDate getRequestedFrom() { return requestedFrom; }
    public void setRequestedFrom(LocalDate requestedFrom) { this.requestedFrom = requestedFrom; }
    public LocalDate getRequestedTo() { return requestedTo; }
    public void setRequestedTo(LocalDate requestedTo) { this.requestedTo = requestedTo; }
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    public String getAvailabilityNotes() { return availabilityNotes; }
    public void setAvailabilityNotes(String availabilityNotes) { this.availabilityNotes = availabilityNotes; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public UUID getScheduledAppointmentId() { return scheduledAppointmentId; }
    public void setScheduledAppointmentId(UUID scheduledAppointmentId) { this.scheduledAppointmentId = scheduledAppointmentId; }
    public Instant getContactedAt() { return contactedAt; }
    public void setContactedAt(Instant contactedAt) { this.contactedAt = contactedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
