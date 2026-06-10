package com.iclinical.technology.fhir;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;

@RestControllerAdvice(basePackages = "com.iclinical.technology.fhir")
public class FHIRExceptionHandler {

    @ExceptionHandler(FHIRResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(FHIRResourceNotFoundException exception) {
        var outcome = FHIRJson.resource("OperationOutcome", java.util.UUID.randomUUID());
        outcome.put("issue", List.of(Map.of(
            "severity", "error",
            "code", "not-found",
            "diagnostics", exception.getMessage()
        )));
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .contentType(MediaType.parseMediaType(FHIRController.FHIR_JSON))
            .body(outcome);
    }
}
