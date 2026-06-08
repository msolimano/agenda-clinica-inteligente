package com.iclinical.technology.appointments.waitinglist;

class WaitingListNotFoundException extends RuntimeException {
    WaitingListNotFoundException(String message) {
        super(message);
    }
}
