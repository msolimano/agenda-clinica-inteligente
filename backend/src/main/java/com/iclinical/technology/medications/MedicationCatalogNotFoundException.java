package com.iclinical.technology.medications;

public class MedicationCatalogNotFoundException extends RuntimeException {
    public MedicationCatalogNotFoundException(String message) {
        super(message);
    }
}
