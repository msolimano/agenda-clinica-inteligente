package com.iclinical.technology.documents;

class ClinicalDocumentStorageException extends RuntimeException {
    ClinicalDocumentStorageException(String message) {
        super(message);
    }

    ClinicalDocumentStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
