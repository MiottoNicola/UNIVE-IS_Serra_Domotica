package com.example.SerraDomotica.forecast;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Locale;

public class ForecastRepository {

    public Forecast getForecastForLocation(double latitude, double longitude) throws Exception {
        String language = Locale.getDefault().getLanguage();

        switch (language) {
            case "it":
                language = "it";
                break;
            case "es":
                language = "es";
                break;
            case "fr":
                language = "fr";
                break;
            case "de":
                language = "de";
                break;
            case "pt":
                language = "pt";
                break;
            case "ja":
                language = "ja";
                break;
            default:
                language = "en";
                break;
        }

        String apiKey = "478eff817c63c52301446cb3dbc57a50";
        String urlString = "https://api.openweathermap.org/data/2.5/weather?lat=" + latitude + "&lon=" + longitude + "&appid=" + apiKey + "&units=metric&lang=" + language;
        URL url = new URL(urlString);
        HttpURLConnection urlConnection = null;
        BufferedReader in = null;

        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setConnectTimeout(10000); // 10 seconds timeout for connection
            urlConnection.setReadTimeout(10000); // 10 seconds timeout for reading

            in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            // Parse the JSON
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

            return new Forecast(forecastTitle, forecastID, forecastLatitude, forecastLongitude, forecastDescription, icon, temperature, humidity, windSpeed);

        } catch (SocketTimeoutException e) {
            Log.e("ForecastRepository", "SocketTimeoutException while fetching weather data: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            Log.e("ForecastRepository", "Exception while fetching weather data: " + e.getMessage());
            throw e;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    Log.e("ForecastRepository", "Exception while closing BufferedReader: " + e.getMessage());
                }
            }
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    public String getIconURL(Forecast forecast) {
        return "https://openweathermap.org/img/wn/" + forecast.getIcon() + "@2x.png";
    }
}