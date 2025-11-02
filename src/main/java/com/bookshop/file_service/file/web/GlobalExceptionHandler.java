package com.bookshop.file_service.file.web;
import com.bookshop.file_service.file.domain.DuplicateFileException;
import com.bookshop.file_service.file.domain.FileServiceException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FileNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<String> handleFileNotFoundException(FileNotFoundException ex) {
        return Mono.just(ex.getMessage());
    }

    @ExceptionHandler(DuplicateFileException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Mono<String> handleDuplicateFileException(DuplicateFileException ex) {
        return Mono.just(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<String> handleGenericException(Exception ex) {
        return Mono.just(ex.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<Map<String, String>> handleConstraintViolationException(
            ConstraintViolationException ex) {

        return Mono.fromCallable(() -> {
            Map<String, String> errors = new HashMap<>();
            Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();

            for (ConstraintViolation<?> violation : violations) {
                String fieldName = getFieldNameFromPath(violation.getPropertyPath().toString());
                String errorMessage = violation.getMessage();
                errors.put(fieldName, errorMessage);
            }
            return errors;
        });
    }

    private String getFieldNameFromPath(String propertyPath) {
        String[] parts = propertyPath.split("\\.");
        return parts[parts.length - 1];
    }
}