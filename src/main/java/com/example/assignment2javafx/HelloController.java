package com.example.assignment2javafx;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.concurrent.Task;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpConnectTimeoutException;
import java.net.UnknownHostException;

public class HelloController {

    @FXML
    private Button torontoButton;
    @FXML
    private Button newYorkButton;
    @FXML
    private Button vancouverButton;
    @FXML
    private TextArea resultArea;

    private static final String API_KEY = "fc4d07f50e8e9545e6a7f8369a55131e";
    private static final String WEATHER_API_URL = "https://api.openweathermap.org/data/2.5/weather?lat=%f&lon=%f&appid=%s";

    @FXML
    protected void onTorontoButtonClick() {
        fetchWeatherData(43.65107, -79.347015); // Toronto coordinates
    }

    @FXML
    protected void onNewYorkButtonClick() {
        fetchWeatherData(40.712776, -74.005974); // New York coordinates
    }

    @FXML
    protected void onVancouverButtonClick() {
        fetchWeatherData(49.282729, -123.120738); // Vancouver coordinates
    }

    private void fetchWeatherData(double lat, double lon) {
        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                return fetchWeatherDataFromAPI(lat, lon);
            }
        };

        task.setOnSucceeded(event -> {
            try {
                String jsonResponse = task.getValue();
                WeatherData weatherData = parseJson(jsonResponse);
                if (weatherData != null) {
                    double tempK = weatherData.getMain().getTemp();
                    double tempC = tempK - 273.15; // Convert Kelvin to Celsius
                    double tempF = (tempK - 273.15) * 9/5 + 32; // Convert Kelvin to Fahrenheit
                    resultArea.setText(String.format(
                            "Temperature: %.2f°C / %.2f°F\n" +
                                    "Humidity: %d%%\n" +
                                    "Wind Speed: %.2f m/s\n" +
                                    "Description: %s",
                            tempC, tempF,
                            weatherData.getMain().getHumidity(),
                            weatherData.getWind().getSpeed(),
                            weatherData.getWeather().get(0).getDescription()
                    ));
                } else {
                    resultArea.setText("Weather data is not available.");
                }
            } catch (Exception e) {
                resultArea.setText("Error: " + e.getMessage());
            }
        });

        task.setOnFailed(event -> {
            Throwable exception = task.getException();
            if (exception instanceof UnknownHostException || exception instanceof HttpConnectTimeoutException) {
                resultArea.setText("No Internet Connection");
            } else {
                resultArea.setText("Error: " + exception.getMessage());
            }
        });

        new Thread(task).start();
    }

    private String fetchWeatherDataFromAPI(double lat, double lon) throws IOException, InterruptedException {
        String url = String.format(WEATHER_API_URL, lat, lon, API_KEY);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    private WeatherData parseJson(String json) {
        Gson gson = new Gson();
        WeatherData weatherData = gson.fromJson(json, WeatherData.class);
        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
        System.out.println("Raw JSON: " + jsonObject);

        return weatherData;
    }
}
