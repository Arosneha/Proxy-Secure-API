
package com.example.proxy_service.Dto;

public class WeatherSearchResponse {

    private String location;
    private String country;
    private double latitude;
    private double longitude;
    private double temperature;
    private String description;

    public WeatherSearchResponse(
            String location,
            String country,
            double latitude,
            double longitude,
            double temperature,
            String description) {

        this.location = location;
        this.country = country;
        this.latitude = latitude;
        this.longitude = longitude;
        this.temperature = temperature;
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public String getCountry() {
        return country;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getTemperature() {
        return temperature;
    }

    public String getDescription() {
        return description;
    }
}
