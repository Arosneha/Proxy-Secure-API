package com.example.proxy_service.Client;

import com.example.proxy_service.Dto.GeocodingResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import com.example.proxy_service.Exception.WeatherApiException;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

import java.net.SocketTimeoutException;

import java.util.List;

@Component
public class GeocodingClient {

    private final RestClient restClient;
    private final String apiKey;

    public GeocodingClient(
            RestClient.Builder restClientBuilder,
            @Value("${weather.api.base-url}") String baseUrl,
            @Value("${weather.api.key}") String apiKey) {

        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();

        this.apiKey = apiKey;
    }

    public List<GeocodingResponse> getCoordinates(String city) {

        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/geo/1.0/direct")
                            .queryParam("q", city)
                            .queryParam("limit", 1)
                            .queryParam("appid", apiKey)
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<GeocodingResponse>>() {});

        } catch (RestClientResponseException | ResourceAccessException exception) {
            throw handleGeocodingApiError(exception);
        }
    }

    private WeatherApiException handleGeocodingApiError(Exception exception) {

        if (exception instanceof RestClientResponseException responseException) {

            HttpStatus status = switch (responseException.getStatusCode().value()) {
                case 404 -> HttpStatus.NOT_FOUND;
                case 429 -> HttpStatus.TOO_MANY_REQUESTS;
                case 401 -> HttpStatus.BAD_GATEWAY;
                default -> HttpStatus.BAD_GATEWAY;
            };

            return new WeatherApiException(
                    status,
                    "Location search provider is currently unavailable."
            );
        }

        if (exception instanceof ResourceAccessException resourceException) {

            if (resourceException.getCause() instanceof SocketTimeoutException) {
                return new WeatherApiException(
                        HttpStatus.GATEWAY_TIMEOUT,
                        "Location search request timed out."
                );
            }

            return new WeatherApiException(
                    HttpStatus.BAD_GATEWAY,
                    "Unable to connect to location search provider."
            );
        }

        return new WeatherApiException(
                HttpStatus.BAD_GATEWAY,
                "An unexpected location search error occurred."
        );
    }

}