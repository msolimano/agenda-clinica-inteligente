package com.iclinical.technology.appointments;

public class AppointmentValidationException extends RuntimeException {
    public AppointmentValidationException(String message) {
        super(message);
    }
}
