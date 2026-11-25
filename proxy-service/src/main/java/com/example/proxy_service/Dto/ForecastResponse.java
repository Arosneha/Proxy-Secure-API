package com.example.proxy_service.Dto;

import java.time.LocalDateTime;

public class ForecastResponse {

    private LocalDateTime forecastTime;
    private double temperature;
    private double feelsLike;
    private int humidity;
    private String description;

    public ForecastResponse(
            LocalDateTime forecastTime,
            double temperature,
            double feelsLike,
            int humidity,
            String description) {

        this.forecastTime = forecastTime;
        this.temperature = temperature;
        this.feelsLike = feelsLike;
        this.humidity = humidity;
        this.description = description;
    }

    public LocalDateTime getForecastTime() {
        return forecastTime;
    }

    public double getTemperature() {
        return temperature;
    }

    public double getFeelsLike() {
        return feelsLike;
    }

    public int getHumidity() {
        return humidity;
    }

    public String getDescription() {
        return description;
    }
}