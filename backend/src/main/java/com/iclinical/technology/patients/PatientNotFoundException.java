package com.iclinical.technology.patients;

class PatientNotFoundException extends RuntimeException {
    PatientNotFoundException(String message) {
        super(message);
    }
}
