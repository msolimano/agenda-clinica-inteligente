package com.iclinical.technology.documents;

class ClinicalDocumentNotFoundException extends RuntimeException {
    ClinicalDocumentNotFoundException(String message) {
        super(message);
    }
}
