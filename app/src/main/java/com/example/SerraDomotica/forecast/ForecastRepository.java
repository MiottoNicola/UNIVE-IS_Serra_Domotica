package com.example.SerraDomotica.forecast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class ForecastRepository {
    private String apiKey = "478eff817c63c52301446cb3dbc57a50";

    // Metodo per ottenere il forecast tramite latitudine e longitudine
    public Forecast getForecastForLocation(double latitude, double longitude) throws Exception {
        String urlString = "https://api.openweathermap.org/data/2.5/weather?lat=" + latitude + "&lon=" + longitude + "&appid=" + apiKey + "&units=metric&lang=en";
        URL url = new URL(urlString);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        // Parse del JSON
        JSONObject jsonData = new JSONObject(response.toString());
        JSONArray weatherArray = jsonData.getJSONArray("weather");
        JSONObject weather = weatherArray.getJSONObject(0);

        String forecastLatitude = jsonData.getJSONObject("coord").getString("lat");
        String forecastLongitude = jsonData.getJSONObject("coord").getString("lon");
        String forecastTitle = weather.getString("main");
        int forecastID = weather.getInt("id");
        String forecastDescription = weather.getString("description");
        double temperature = jsonData.getJSONObject("main").getDouble("temp");
        int humidity = jsonData.getJSONObject("main").getInt("humidity");
        double windSpeed = jsonData.getJSONObject("wind").getDouble("speed");
        String icon = weather.getString("icon");

        // Crea e restituisce un oggetto Forecast
        return new Forecast(forecastTitle, forecastID, forecastLatitude, forecastLongitude,forecastDescription, icon, temperature, humidity, windSpeed);
    }

    // Metodo per ottenere l'URL dell'icona
    public String getIconURL(Forecast forecast) {
        return "https://openweathermap.org/img/wn/" + forecast.getIcon() + "@2x.png";
    }
}
