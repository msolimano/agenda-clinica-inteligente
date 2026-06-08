package com.iclinical.technology.clinicalrecords;

class ClinicalRecordNotFoundException extends RuntimeException {
    ClinicalRecordNotFoundException(String message) {
        super(message);
    }
}
