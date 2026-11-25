package com.example.proxy_service.Dto;

public class CurrentWeatherResponse {

    private String location;
    private String country;
    private double temperature;
    private double feelsLike;
    private int humidity;
    private String description;
    private double windSpeed;
    private int visibility;

    public CurrentWeatherResponse(
            String location,
            String country,
            double temperature,
            double feelsLike,
            int humidity,
            String description,
            double windSpeed,
            int visibility) {

        this.location = location;
        this.country = country;
        this.temperature = temperature;
        this.feelsLike = feelsLike;
        this.humidity = humidity;
        this.description = description;
        this.windSpeed = windSpeed;
        this.visibility = visibility;
    }

    public String getLocation() {
        return location;
    }

    public String getCountry() {
        return country;
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

    public double getWindSpeed() {
        return windSpeed;
    }

    public int getVisibility() {
        return visibility;
    }
}
