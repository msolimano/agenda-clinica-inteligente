package com.iclinical.technology.ai;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice(basePackages = "com.iclinical.technology.ai")
class AIAnalysisModuleExceptionHandler {

    @ExceptionHandler(AIAnalysisNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ApiError handleNotFound(AIAnalysisNotFoundException exception) {
        return new ApiError(exception.getMessage(), Map.of());
    }

    @ExceptionHandler(AIAnalysisValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ApiError handleValidation(AIAnalysisValidationException exception) {
        return new ApiError(exception.getMessage(), Map.of());
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
