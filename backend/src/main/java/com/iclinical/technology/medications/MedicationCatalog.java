package com.iclinical.technology.medications;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "medication_catalog")
public class MedicationCatalog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "organization_id")
    private UUID organizationId;

    @Column(name = "medication_code", length = 80)
    private String medicationCode;

    @Column(name = "medication_code_system", length = 80)
    private String medicationCodeSystem;

    @Column(name = "medication_name", nullable = false, length = 255)
    private String medicationName;

    @Column(name = "active_ingredient", length = 255)
    private String activeIngredient;

    @Column(length = 180)
    private String presentation;

    @Column(length = 120)
    private String strength;

    @Column(name = "pharmaceutical_form", length = 120)
    private String pharmaceuticalForm;

    @Column(length = 120)
    private String route;

    @Column(length = 180)
    private String manufacturer;

    @Column(nullable = false, length = 30)
    private String status = "active";

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
    public String getMedicationCode() { return medicationCode; }
    public void setMedicationCode(String medicationCode) { this.medicationCode = medicationCode; }
    public String getMedicationCodeSystem() { return medicationCodeSystem; }
    public void setMedicationCodeSystem(String medicationCodeSystem) { this.medicationCodeSystem = medicationCodeSystem; }
    public String getMedicationName() { return medicationName; }
    public void setMedicationName(String medicationName) { this.medicationName = medicationName; }
    public String getActiveIngredient() { return activeIngredient; }
    public void setActiveIngredient(String activeIngredient) { this.activeIngredient = activeIngredient; }
    public String getPresentation() { return presentation; }
    public void setPresentation(String presentation) { this.presentation = presentation; }
    public String getStrength() { return strength; }
    public void setStrength(String strength) { this.strength = strength; }
    public String getPharmaceuticalForm() { return pharmaceuticalForm; }
    public void setPharmaceuticalForm(String pharmaceuticalForm) { this.pharmaceuticalForm = pharmaceuticalForm; }
    public String getRoute() { return route; }
    public void setRoute(String route) { this.route = route; }
    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
