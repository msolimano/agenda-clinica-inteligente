package com.iclinical.technology.appointments;

class AppointmentValidationException extends RuntimeException {
    AppointmentValidationException(String message) {
        super(message);
    }
}
