
package com.example.proxy_service.Controller;

import com.example.proxy_service.Dto.CurrentWeatherResponse;
import com.example.proxy_service.Dto.WeatherSearchResponse;
import com.example.proxy_service.Service.WeatherService;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.example.proxy_service.Dto.ForecastResponse;
import java.util.List;

@RestController
@RequestMapping("/api/weather")
@Validated
public class WeatherTestController {

    private final WeatherService weatherService;

    public WeatherTestController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @PreAuthorize("hasAnyRole('USER', 'PREMIUM')")
    @GetMapping("/current")
    public CurrentWeatherResponse getCurrentWeather(

            @RequestParam
            @DecimalMin(value = "-90.0")
            @DecimalMax(value = "90.0")
            double lat,

            @RequestParam
            @DecimalMin(value = "-180.0")
            @DecimalMax(value = "180.0")
            double lon,

            @AuthenticationPrincipal Jwt jwt) {

        String userEmail = jwt.getSubject();

        return weatherService.getCurrentWeather(
                lat,
                lon,
                userEmail
        );
    }
    @PreAuthorize("hasAnyRole('USER', 'PREMIUM')")
    @GetMapping("/search")
    public WeatherSearchResponse searchWeatherByCity(
            @RequestParam
            @jakarta.validation.constraints.NotBlank
            @jakarta.validation.constraints.Size(max = 100)
            String city,

            @AuthenticationPrincipal Jwt jwt) {

        String userEmail = jwt.getSubject();

        return weatherService.getWeatherByCity(
                city.trim(),
                userEmail
        );
    }
    @PreAuthorize("hasRole('PREMIUM')")
    @GetMapping("/forecast")
    public List<ForecastResponse> getFiveDayForecast(
            @RequestParam
            @jakarta.validation.constraints.DecimalMin("-90.0")
            @jakarta.validation.constraints.DecimalMax("90.0")
            double lat,

            @RequestParam
            @jakarta.validation.constraints.DecimalMin("-180.0")
            @jakarta.validation.constraints.DecimalMax("180.0")
            double lon,

            @AuthenticationPrincipal Jwt jwt) {

        String userEmail = jwt.getSubject();

        return weatherService.getFiveDayForecast(
                lat,
                lon,
                userEmail
        );
    }
}
