package com.iclinical.technology.clinicalprescriptions;

public class ClinicalPrescriptionNotFoundException extends RuntimeException {
    public ClinicalPrescriptionNotFoundException(String message) {
        super(message);
    }
}
