package com.iclinical.technology.clinicaldiagnoses;

public class ClinicalDiagnosisNotFoundException extends RuntimeException {
    public ClinicalDiagnosisNotFoundException(String message) {
        super(message);
    }
}
