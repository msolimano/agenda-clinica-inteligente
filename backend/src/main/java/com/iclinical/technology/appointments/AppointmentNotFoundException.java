package com.iclinical.technology.appointments;

class AppointmentNotFoundException extends RuntimeException {
    AppointmentNotFoundException(String message) {
        super(message);
    }
}
