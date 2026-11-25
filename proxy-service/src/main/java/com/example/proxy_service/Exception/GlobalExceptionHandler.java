package com.example.proxy_service.Exception;


import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(
            ConstraintViolationException ex) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "error", "Invalid request parameters",
                        "message", "Please provide valid latitude and longitude."
                ));
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(
            IllegalArgumentException exception) {

        return ResponseEntity
                .badRequest()
                .body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(WeatherApiException.class)
    public ResponseEntity<Map<String, String>> handleWeatherApiException(
            WeatherApiException exception) {

        return ResponseEntity
                .status(exception.getStatus())
                .body(Map.of("message", exception.getMessage()));
    }

}
