package com.example.proxy_service.Service;

import com.example.proxy_service.Client.OpenWeatherClient;
import com.example.proxy_service.Dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.example.proxy_service.Client.GeocodingClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.stream.Collectors;
import java.util.List;

@Service
public class WeatherService {

    private final OpenWeatherClient openWeatherClient;
    private final UsageTrackingService usageTrackingService;
    private final GeocodingClient geocodingClient;

    public WeatherService(
            OpenWeatherClient openWeatherClient,
            UsageTrackingService usageTrackingService,
            GeocodingClient geocodingClient) {

        this.openWeatherClient = openWeatherClient;
        this.usageTrackingService = usageTrackingService;
        this.geocodingClient = geocodingClient;
    }

    public CurrentWeatherResponse getCurrentWeather(
            double latitude,
            double longitude,
            String userEmail) {

        usageTrackingService.recordRequest(userEmail);

        OpenWeatherCurrentResponse weather =
                openWeatherClient.getCurrentWeather(latitude, longitude);

        return new CurrentWeatherResponse(
                weather.getName(),
                weather.getSys().getCountry(),
                weather.getMain().getTemp(),
                weather.getMain().getFeelsLike(),
                weather.getMain().getHumidity(),
                weather.getWeather().get(0).getDescription(),
                weather.getWind().getSpeed(),
                weather.getVisibility()
        );
    }
    public WeatherSearchResponse getWeatherByCity(
            String city,
            String userEmail){

        List<GeocodingResponse> locations =
                geocodingClient.getCoordinates(city);

        if (locations == null || locations.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "City not found: " + city
            );
        }

        GeocodingResponse location = locations.get(0);

        usageTrackingService.recordRequest(userEmail);

        OpenWeatherCurrentResponse weather =
                openWeatherClient.getCurrentWeather(
                        location.getLat(),
                        location.getLon()
                );

        return new WeatherSearchResponse(
                location.getName(),
                location.getCountry(),
                location.getLat(),
                location.getLon(),
                weather.getMain().getTemp(),
                weather.getWeather().get(0).getDescription()
        );
    }
    public List<ForecastResponse> getFiveDayForecast(
            double latitude,
            double longitude,
            String userEmail) {

        usageTrackingService.recordRequest(userEmail);

        OpenWeatherForecastResponse forecast =
                openWeatherClient.getFiveDayForecast(latitude, longitude);

        return forecast.getList()
                .stream()
                .map(item -> new ForecastResponse(
                        LocalDateTime.ofInstant(
                                Instant.ofEpochSecond(item.getDt()),
                                ZoneId.of("UTC")
                        ),
                        item.getMain().getTemp(),
                        item.getMain().getFeels_like(),
                        item.getMain().getHumidity(),
                        item.getWeather().get(0).getDescription()
                ))
                .collect(Collectors.toList());
    }
}