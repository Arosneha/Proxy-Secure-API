
package com.example.proxy_service.Client;

import com.example.proxy_service.Exception.WeatherApiException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.net.SocketTimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.*;

class OpenWeatherClientTest {

    private OpenWeatherClient openWeatherClient;

    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {

        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://api.openweathermap.org");

        server = MockRestServiceServer.bindTo(builder).build();

        openWeatherClient = new OpenWeatherClient(
                builder.build(),
                "test-api-key"
        );
    }

    @Test
    void shouldMapUnauthorizedApiKeyToBadGateway() {

        server.expect(requestTo(
                org.hamcrest.Matchers.containsString(
                        "/data/2.5/weather"
                )
        )).andRespond(withStatus(UNAUTHORIZED));

        WeatherApiException exception = assertThrows(
                WeatherApiException.class,
                () -> openWeatherClient.getCurrentWeather(9.9, 78.1)
        );

        assertEquals(BAD_GATEWAY, exception.getStatus());

        server.verify();
    }

    @Test
    void shouldMapNotFoundToNotFound() {

        server.expect(requestTo(
                org.hamcrest.Matchers.containsString(
                        "/data/2.5/weather"
                )
        )).andRespond(withStatus(NOT_FOUND));

        WeatherApiException exception = assertThrows(
                WeatherApiException.class,
                () -> openWeatherClient.getCurrentWeather(9.9, 78.1)
        );

        assertEquals(NOT_FOUND, exception.getStatus());

        server.verify();
    }

    @Test
    void shouldMapRateLimitToTooManyRequests() {

        server.expect(requestTo(
                org.hamcrest.Matchers.containsString(
                        "/data/2.5/weather"
                )
        )).andRespond(withStatus(TOO_MANY_REQUESTS));

        WeatherApiException exception = assertThrows(
                WeatherApiException.class,
                () -> openWeatherClient.getCurrentWeather(9.9, 78.1)
        );

        assertEquals(TOO_MANY_REQUESTS, exception.getStatus());

        server.verify();
    }

    @Test
    void shouldMapTimeoutToGatewayTimeout() {

        server.expect(requestTo(
                org.hamcrest.Matchers.containsString(
                        "/data/2.5/weather"
                )
        )).andRespond(withException(
                new SocketTimeoutException("Connection timed out")
        ));

        WeatherApiException exception = assertThrows(
                WeatherApiException.class,
                () -> openWeatherClient.getCurrentWeather(9.9, 78.1)
        );

        assertEquals(GATEWAY_TIMEOUT, exception.getStatus());

        server.verify();
    }
}
