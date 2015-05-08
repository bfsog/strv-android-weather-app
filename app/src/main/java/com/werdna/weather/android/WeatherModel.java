package com.werdna.weather.android;


import android.graphics.Bitmap;
import android.util.Log;

/*
 Class for storing weather summary and forecast information
 Author: Andrew Buchan
 Creation Date: 2015-05-06
 Modified Date: 2015-05-06
 */
public class WeatherModel {

    Double temperature;
    String summary;
    String icon;
    Bitmap weatherIcon;
    String bearing;
    Double windSpeed;
    Double humidity;
    Double precipitation;
    Double pressure;
    String dayOfWeek;

    double ConvertKelvinToCelsius(Double kelvinValue)
    {
        return Math.round(kelvinValue - 273.15);
    }

    String formatBearing(double bearing) {
        if (bearing < 0 && bearing > -180) {
            // Normalize to [0,360]
            bearing = 360.0 + bearing;
        }
        if (bearing > 360 || bearing < -180) {
            return "Unknown";
        }

        String directions[] = {
                "N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE",
                "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW",
                "N"};

        return directions[(int) Math.floor(((bearing + 11.25) % 360) / 22.5)];
    }

}
