
package com.example.proxy_service.Exception;

import com.example.proxy_service.Exception.WeatherApiException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler =
            new GlobalExceptionHandler();

    @Test
    void shouldReturnStatusStoredInWeatherApiException() {

        WeatherApiException exception = new WeatherApiException(
                HttpStatus.NOT_FOUND,
                "Weather resource not found"
        );

        ResponseEntity<Map<String, String>> response =
                handler.handleWeatherApiException(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(
                "Weather resource not found",
                response.getBody().get("message")
        );
    }
}
