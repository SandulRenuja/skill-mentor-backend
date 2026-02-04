package com.stemlink.skillmentor.controllers;

import com.stemlink.skillmentor.dto.ErrorResponse;
import com.stemlink.skillmentor.exceptions.SkillMentorException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class AbstractController {

    // reusable methods and logic
    protected <T> ResponseEntity<T> sendOkResponse(T response) {
        return ResponseEntity.ok(response);
    }

    protected <T> ResponseEntity<T> sendCreatedResponse(T response) {
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    protected <T> ResponseEntity<T> sendNotFoundResponse() {
        return ResponseEntity.notFound().build();
    }

    protected <T> ResponseEntity<T> sendNoContentResponse() {
        return ResponseEntity.noContent().build();
    }

    // Handle Security/Auth errors correctly
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        log.error("Access Denied: {}", ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.builder()
                .message("You do not have permission to perform this action.")
                .errorCode("FORBIDDEN")
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }
    // handle error messages

    // common exceptions handling
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex) {
        log.error("Unexpected error: {}" ,ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.builder()
                .message("An unexpected error occurred")
                .errorCode("INTERNAL SERVER ERROR")
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    //    Handles Custom SkillMentor Exceptions
    @ExceptionHandler(SkillMentorException.class)
    public ResponseEntity<ErrorResponse> handleSkillMentorException(
            SkillMentorException ex) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .message(ex.getMessage())
                .errorCode(ex.getStatus().toString())
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                .build();

        return new ResponseEntity<>(errorResponse, ex.getStatus());
    }

    // Validation Exception
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });


        ErrorResponse errorResponse = ErrorResponse.builder()
                .message("Validation failed")
                .errorCode("BAD REQUEST")
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                .validationErrors(errors)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}
