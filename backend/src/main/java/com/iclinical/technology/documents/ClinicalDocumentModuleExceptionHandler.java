package com.iclinical.technology.documents;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice(basePackages = "com.iclinical.technology.documents")
class ClinicalDocumentModuleExceptionHandler {

    @ExceptionHandler(ClinicalDocumentNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ApiError handleNotFound(ClinicalDocumentNotFoundException exception) {
        return new ApiError(exception.getMessage(), Map.of());
    }

    @ExceptionHandler({ClinicalDocumentValidationException.class, ClinicalDocumentStorageException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ApiError handleValidation(RuntimeException exception) {
        return new ApiError(exception.getMessage(), Map.of());
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, MissingServletRequestPartException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ApiError handleMissingRequestPart(Exception exception) {
        return new ApiError("La solicitud debe incluir metadata y archivo clinico", Map.of());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ApiError handleMaxUploadSize() {
        return new ApiError("El archivo supera el tamano maximo permitido", Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ApiError handleInvalidArgument(MethodArgumentNotValidException exception) {
        var fields = exception.getBindingResult()
            .getFieldErrors()
            .stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                error -> error.getDefaultMessage() == null ? "Valor invalido" : error.getDefaultMessage(),
                (first, second) -> first
            ));

        return new ApiError("La solicitud contiene datos invalidos", fields);
    }

    record ApiError(String message, Map<String, String> fields) {
    }
}
