package com.example.proxy_service.Client;

import com.example.proxy_service.Dto.OpenWeatherForecastResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import com.example.proxy_service.Dto.OpenWeatherCurrentResponse;
import com.example.proxy_service.Exception.WeatherApiException;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

import java.net.SocketTimeoutException;

@Component
public class OpenWeatherClient {

    private final RestClient weatherRestClient;
    private final String apiKey;

    public OpenWeatherClient(
            RestClient weatherRestClient,
            @Value("${weather.api.key}") String apiKey) {

        this.weatherRestClient = weatherRestClient;
        this.apiKey = apiKey;
    }
    public OpenWeatherCurrentResponse getCurrentWeather(
            double latitude,
            double longitude) {

        try {
            return weatherRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/data/2.5/weather")
                            .queryParam("lat", latitude)
                            .queryParam("lon", longitude)
                            .queryParam("appid", apiKey)
                            .queryParam("units", "metric")
                            .build())
                    .retrieve()
                    .body(OpenWeatherCurrentResponse.class);

        } catch (RestClientResponseException | ResourceAccessException exception) {
            throw handleWeatherApiError(exception);
        }
    }

    public OpenWeatherForecastResponse getFiveDayForecast(
            double latitude,
            double longitude) {

        try {
            return weatherRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/data/2.5/forecast")
                            .queryParam("lat", latitude)
                            .queryParam("lon", longitude)
                            .queryParam("appid", apiKey)
                            .queryParam("units", "metric")
                            .build())
                    .retrieve()
                    .body(OpenWeatherForecastResponse.class);

        } catch (RestClientResponseException | ResourceAccessException exception) {
            throw handleWeatherApiError(exception);
        }
    }

    private WeatherApiException handleWeatherApiError(Exception exception) {

        if (exception instanceof RestClientResponseException responseException) {

            HttpStatus status = switch (responseException.getStatusCode().value()) {
                case 404 -> HttpStatus.NOT_FOUND;
                case 429 -> HttpStatus.TOO_MANY_REQUESTS;
                case 401 -> HttpStatus.BAD_GATEWAY;
                default -> HttpStatus.BAD_GATEWAY;
            };


            return new WeatherApiException(
                    status,
                    "Weather provider is currently unavailable."
            );
        }

        if (exception instanceof ResourceAccessException resourceException) {

            if (resourceException.getCause() instanceof SocketTimeoutException) {
                return new WeatherApiException(
                        HttpStatus.GATEWAY_TIMEOUT,
                        "Weather provider request timed out."
                );
            }

            return new WeatherApiException(
                    HttpStatus.BAD_GATEWAY,
                    "Unable to connect to weather provider."
            );
        }

        return new WeatherApiException(
                HttpStatus.BAD_GATEWAY,
                "An unexpected weather provider error occurred."
        );
    }

}


