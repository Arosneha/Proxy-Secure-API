package com.example.proxy_service.Dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class OpenWeatherCurrentResponse {

    private String name;
    private Main main;
    private List<Weather> weather;
    private Wind wind;
    private int visibility;
    private Sys sys;

    public String getName() {
        return name;
    }

    public Main getMain() {
        return main;
    }

    public List<Weather> getWeather() {
        return weather;
    }

    public Wind getWind() {
        return wind;
    }

    public int getVisibility() {
        return visibility;
    }

    public Sys getSys() {
        return sys;
    }

    public static class Main {

        private double temp;

        @JsonProperty("feels_like")
        private double feelsLike;

        private int humidity;

        public double getTemp() {
            return temp;
        }

        public double getFeelsLike() {
            return feelsLike;
        }

        public int getHumidity() {
            return humidity;
        }
    }

    public static class Weather {

        private String description;

        public String getDescription() {
            return description;
        }
    }

    public static class Wind {

        private double speed;

        public double getSpeed() {
            return speed;
        }
    }

    public static class Sys {

        private String country;

        public String getCountry() {
            return country;
        }
    }
}
