package com.iclinical.technology.professionals;

class ProfessionalNotFoundException extends RuntimeException {
    ProfessionalNotFoundException(String message) {
        super(message);
    }
}
