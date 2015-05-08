package com.werdna.weather.android;

/*
 Class for defining constants which are used throughout this application:
 Author: Andrew Buchan
 Creation Date: 2015-05-06
 Modified Date: 2015-05-06
 */
public class Constants {


    /*
      API CONSTANTS
      These constants are for communicating with the OpenWeather API
     */
    // Used in the "Today" fragment. Used to get weather summary for current day
    public final static String TODAY_WEATHER_UNFORMATTED_URL = "http://api.openweathermap.org/data/2.5/weather?q=%s,%s&units=metric";

    // Used in the "Forecast" fragment. Used to get weather forcast for next 4 days (including today)
    public final static String FORECAST_WEATHER_UNFORMATTED_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?q=%s,%s&mode=json&units=metric&cnt=%s";

    // Used to get the image associated with all retrieved data from both today and forecast data
    public final static String WEATHER_ICON_UNFORMATTED_URL = "http://openweathermap.org/img/w/%s.png";

    // Used to dictate how many days forecast to retrieve. Changing this will change the Forecast
    // fragment. Max is 16.
    public final static int FORECAST_NUMBER_OF_DAYS = 4;

    /*
      UI CONSTANTS
      These constants are values which are displayed to the user/used in calculations. They are
      // standard units of measurements/speed/etc so will not change
     */
    public final static String PERCENT_SYMBOL = "%";
    public final static String MILLIMETRES_SYMBOL = "mm";
    public final static String PRESSURE_SYMBOL = "hPa";
    public final static String KILOMETRES_PER_HOUR_SYMBOL = "km/h";
    public final static Double CALCULATING_WIND_SPEED = 3.6;
    public final static String DEGREE_SYMBOL = "\u2103";
    public final static String ABOUT_DIALOG_TITLE = "About";
    public final static String ABOUT_DIALOG_TEXT = "This is Android test project made by Andrew Buchan\n\n";
    public final static String ABOUT_DIALOG_CONFIRM = "OK";
    public final static String ERROR_DIALOG_TITLE = "Error";
    public final static String ERROR_DIALOG_TEXT = "Unable to get device location.";

    /*
      CACHE CONSTANTS
      These constants are used in caching data retrieved from API
     */
    public final static String PREFERENCES_FILE_NAME = "com.werdna.weather.android.data_file";
    public final static String PREFERENCES_FORECAST_RESPONSE_EXISTS_KEY = "ForecastResponseExists";
    public final static String PREFERENCES_FORECAST_DATE_REFRESH_IN_MS_KEY = "ForecastRefreshInMS";
    public final static String PREFERENCES_FORECAST_RESPONSE_KEY = "ForecastResponse";

}
