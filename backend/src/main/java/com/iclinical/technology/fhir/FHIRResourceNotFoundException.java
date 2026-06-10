package com.iclinical.technology.fhir;

public class FHIRResourceNotFoundException extends RuntimeException {
    public FHIRResourceNotFoundException(String message) {
        super(message);
    }
}
