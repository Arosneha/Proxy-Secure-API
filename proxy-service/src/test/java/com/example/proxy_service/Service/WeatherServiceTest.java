
package com.example.proxy_service.Service;

import com.example.proxy_service.Client.OpenWeatherClient;
import com.example.proxy_service.Client.GeocodingClient;
import com.example.proxy_service.Dto.OpenWeatherCurrentResponse;
import com.example.proxy_service.Dto.OpenWeatherForecastResponse;
import com.example.proxy_service.Dto.CurrentWeatherResponse;
import com.example.proxy_service.Dto.GeocodingResponse;
import com.example.proxy_service.Dto.WeatherSearchResponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeatherServiceTest {

    @Mock
    private OpenWeatherClient openWeatherClient;

    @Mock
    private UsageTrackingService usageTrackingService;

    @Mock
    private GeocodingClient geocodingClient;

    @InjectMocks
    private WeatherService weatherService;

    @Test
    void shouldThrowExceptionWhenCityIsNotFound() {

        when(geocodingClient.getCoordinates("UnknownCity"))
                .thenReturn(Collections.emptyList());

        assertThrows(
                ResponseStatusException.class,
                () -> weatherService.getWeatherByCity(
                        "UnknownCity",
                        "user@example.com"
                )
        );

        verify(usageTrackingService, never())
                .recordRequest(anyString());

        verifyNoInteractions(openWeatherClient);
    }

    @Test
    void shouldReturnCurrentWeatherAndRecordUsage() {

        String email = "user@example.com";
        double latitude = 9.9252;
        double longitude = 78.1198;

        OpenWeatherCurrentResponse weather =
                mock(OpenWeatherCurrentResponse.class);

        OpenWeatherCurrentResponse.Main main =
                mock(OpenWeatherCurrentResponse.Main.class);

        OpenWeatherCurrentResponse.Sys sys =
                mock(OpenWeatherCurrentResponse.Sys.class);

        OpenWeatherCurrentResponse.Wind wind =
                mock(OpenWeatherCurrentResponse.Wind.class);

        OpenWeatherCurrentResponse.Weather condition =
                mock(OpenWeatherCurrentResponse.Weather.class);

        when(weather.getName()).thenReturn("Madurai");
        when(weather.getMain()).thenReturn(main);
        when(weather.getSys()).thenReturn(sys);
        when(weather.getWind()).thenReturn(wind);
        when(weather.getWeather()).thenReturn(List.of(condition));

        when(main.getTemp()).thenReturn(32.5);
        when(main.getFeelsLike()).thenReturn(35.0);
        when(main.getHumidity()).thenReturn(65);

        when(sys.getCountry()).thenReturn("IN");
        when(wind.getSpeed()).thenReturn(3.5);
        when(condition.getDescription()).thenReturn("clear sky");

        when(weather.getVisibility()).thenReturn(10000);

        when(openWeatherClient.getCurrentWeather(latitude, longitude))
                .thenReturn(weather);

        CurrentWeatherResponse response =
                weatherService.getCurrentWeather(
                        latitude,
                        longitude,
                        email
                );

        assertEquals("Madurai", response.getLocation());
        assertEquals("IN", response.getCountry());
        assertEquals(32.5, response.getTemperature());
        assertEquals(35.0, response.getFeelsLike());
        assertEquals(65, response.getHumidity());
        assertEquals("clear sky", response.getDescription());
        assertEquals(3.5, response.getWindSpeed());
        assertEquals(10000, response.getVisibility());

        verify(usageTrackingService, times(1))
                .recordRequest(email);

        verify(openWeatherClient, times(1))
                .getCurrentWeather(latitude, longitude);
    }

    @Test
    void shouldReturnWeatherForValidCityAndRecordUsage() {

        String city = "Madurai";
        String email = "user@example.com";

        double latitude = 9.9252;
        double longitude = 78.1198;

        GeocodingResponse location = new GeocodingResponse();
        location.setName("Madurai");
        location.setCountry("IN");
        location.setLat(latitude);
        location.setLon(longitude);

        when(geocodingClient.getCoordinates(city))
                .thenReturn(List.of(location));

        OpenWeatherCurrentResponse weather =
                mock(OpenWeatherCurrentResponse.class);

        OpenWeatherCurrentResponse.Main main =
                mock(OpenWeatherCurrentResponse.Main.class);

        OpenWeatherCurrentResponse.Weather condition =
                mock(OpenWeatherCurrentResponse.Weather.class);

        when(weather.getMain()).thenReturn(main);
        when(weather.getWeather()).thenReturn(List.of(condition));

        when(main.getTemp()).thenReturn(32.5);
        when(condition.getDescription()).thenReturn("clear sky");

        when(openWeatherClient.getCurrentWeather(latitude, longitude))
                .thenReturn(weather);

        WeatherSearchResponse response =
                weatherService.getWeatherByCity(city, email);

        assertNotNull(response);
        assertEquals("Madurai", response.getLocation());
        assertEquals("IN", response.getCountry());
        assertEquals(latitude, response.getLatitude());
        assertEquals(longitude, response.getLongitude());
        assertEquals(32.5, response.getTemperature());
        assertEquals("clear sky", response.getDescription());

        verify(geocodingClient, times(1))
                .getCoordinates(city);

        verify(openWeatherClient, times(1))
                .getCurrentWeather(latitude, longitude);

        verify(usageTrackingService, times(1))
                .recordRequest(email);
    }

    @Test
    void shouldReturnFiveDayForecastAndRecordUsage() {

        String email = "user@example.com";

        long timestamp = 1790000000L;

        OpenWeatherForecastResponse forecast =
                new OpenWeatherForecastResponse();

        OpenWeatherForecastResponse.ForecastItem item =
                new OpenWeatherForecastResponse.ForecastItem();

        OpenWeatherForecastResponse.Main main =
                new OpenWeatherForecastResponse.Main();

        main.setTemp(30.5);
        main.setFeels_like(33.0);
        main.setHumidity(70);

        OpenWeatherForecastResponse.Weather condition =
                new OpenWeatherForecastResponse.Weather();

        condition.setDescription("light rain");

        item.setDt(timestamp);
        item.setMain(main);
        item.setWeather(List.of(condition));

        forecast.setList(List.of(item));

        when(openWeatherClient.getFiveDayForecast(9.9, 78.1))
                .thenReturn(forecast);

        var response = weatherService.getFiveDayForecast(
                9.9,
                78.1,
                email
        );

        assertEquals(1, response.size());
        assertEquals(30.5, response.get(0).getTemperature());
        assertEquals(33.0, response.get(0).getFeelsLike());
        assertEquals(70, response.get(0).getHumidity());
        assertEquals("light rain", response.get(0).getDescription());

        verify(openWeatherClient, times(1))
                .getFiveDayForecast(9.9, 78.1);

        verify(usageTrackingService, times(1))
                .recordRequest(email);
    }
}
