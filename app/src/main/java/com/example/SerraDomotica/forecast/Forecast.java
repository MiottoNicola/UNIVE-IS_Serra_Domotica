package com.example.SerraDomotica.forecast;

public class Forecast {
    private final String forecastTitle;
    private final int forecastID;
    private final String forecastLatitude;
    private final String forecastLongitude;
    private final String forecastDescription;
    private final double temperature;
    private final int humidity;
    private final double windSpeed;
    private final String icon;

    public Forecast(String forecastTitle, int forecastID, String forecastLatitude, String forecastLongitude, String forecastDescription, String icon, double temperature, int humidity, double windSpeed) {
        this.forecastTitle = forecastTitle;
        this.forecastID = forecastID;
        this.forecastLatitude = forecastLatitude;
        this.forecastLongitude = forecastLongitude;
        this.forecastDescription = forecastDescription;
        this.icon = icon;
        this.temperature = temperature;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
    }

    public String getForecastTitle() {
        return forecastTitle;
    }

    public int getForecastID() {
        return forecastID;
    }

    public String getLatitude() {
        return forecastLatitude;
    }

    public String getLongitude() {
        return forecastLongitude;
    }

    public String getForecastDescription() {
        return forecastDescription;
    }

    public double getTemperature() {
        return temperature;
    }

    public int getHumidity() {
        return humidity;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public String getIcon() {
        return icon;
    }
}
