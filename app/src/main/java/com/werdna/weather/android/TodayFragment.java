package com.werdna.weather.android;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;

/*
  Activity fragment to display today's forecast for location retrieved by location services
  Author: Andrew Buchan
  Creation Date: 2015-05-06
  Modified Date: 2015-05-06
 */
public class TodayFragment extends Fragment {

    // Class instance variables
    private WeatherModel _m_currentWeather;
    private LocationModel _m_usersLocation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {

        View rootView = inflater.inflate(R.layout.fragment_today, container, false);

        _m_currentWeather = new WeatherModel();
        _m_usersLocation = new LocationModel().GetLocation(this.getActivity());

        if(_m_usersLocation.retrievedAddress)
        {
            loadData();
        }
        else
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(Constants.ERROR_DIALOG_TEXT)
                    .setCancelable(false)
                    .setTitle(Constants.ERROR_DIALOG_TITLE)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }

        return rootView;
    }

    /*
      Async. task to call OpenWeather API and get today's weather summary
     */
    private class GetTodayWeatherSummary extends AsyncTask<Void, Void, Void>
    {

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }

        private String GetWeatherSummary()
        {
            String response = "";
            WebRequest webRequest = new WebRequest();
            try
            {
                response = webRequest.call(String.format(Constants.TODAY_WEATHER_UNFORMATTED_URL, _m_usersLocation.city, _m_usersLocation.countryCode), WebRequest.GET);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected Void doInBackground(Void... arg0)
        {

            String response = GetWeatherSummary();
            if(response != null)
            {
                try
                {
                    JSONObject summaryObj;
                    JSONArray weatherArr;
                    summaryObj = new JSONObject(response);

                    weatherArr = summaryObj.getJSONArray("weather");
                    JSONObject weather = weatherArr.getJSONObject(0);
                    String summary = weather.getString("main");
                    String icon = weather.getString("icon");

                    weather = summaryObj.getJSONObject("main");
                    Double temperatureInKelvin = weather.getDouble("temp");
                    Double humidity = weather.getDouble("humidity");
                    Double pressure = weather.getDouble("pressure");

                    // Rain does not exist in every response, check it exists before referencing
                    boolean rainExists = weather.has("rain");
                    if(rainExists)
                    {
                        weather = summaryObj.getJSONObject("rain");
                        _m_currentWeather.precipitation = weather.getDouble("3h");
                    }
                    else
                    {
                        _m_currentWeather.precipitation = 0.0;
                    }

                    weather = summaryObj.getJSONObject("wind");
                    _m_currentWeather.windSpeed = weather.getDouble(("speed")) * Constants.CALCULATING_WIND_SPEED;
                    _m_currentWeather.bearing = _m_currentWeather.formatBearing(weather.getDouble("deg"));

                    // Marshall retrieved data into object for UI display
                    _m_currentWeather.temperature = temperatureInKelvin;
                    _m_currentWeather.humidity = humidity;
                    _m_currentWeather.summary = summary;
                    _m_currentWeather.icon = icon;
                    _m_currentWeather.pressure = pressure;

                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {

            super.onPostExecute(result);

            renderView();

            // Grab the icon for this weather summary
            new GetWeatherIcon().execute();

        }

        private void renderView()
        {

            // Reference UI fields for display
            TextView cityAndCountryElm = (TextView)getActivity().findViewById(R.id.city_country_label);
            TextView temperatureElm = (TextView)getActivity().findViewById(R.id.temperature_label);
            TextView summaryElm = (TextView)getActivity().findViewById(R.id.summary_label);
            TextView humidityElm = (TextView)getActivity().findViewById(R.id.humidity_label);
            TextView precipitationElm = (TextView)getActivity().findViewById(R.id.precipitation_label);
            TextView pressureElm = (TextView)getActivity().findViewById(R.id.pressure_label);
            TextView windSpeedElm = (TextView)getActivity().findViewById(R.id.wind_speed_label);
            TextView directionElm = (TextView)getActivity().findViewById(R.id.direction_label);

            // Remove fractions from doubles, cast into ints
            int temperature = (int)Math.round(_m_currentWeather.temperature);
            int humidity = (int)Math.round(_m_currentWeather.humidity);
            int pressure = (int)Math.round(_m_currentWeather.pressure);
            int windSpeed = (int)Math.round(_m_currentWeather.windSpeed);

            // Output values to UI
            cityAndCountryElm.setText(
                    String.format("%s, %s", _m_usersLocation.city, _m_usersLocation.country));

            temperatureElm.setText(String.format("%s%s", temperature, Constants.DEGREE_SYMBOL));
            summaryElm.setText(_m_currentWeather.summary);
            humidityElm.setText(String.format("%s%s", humidity, Constants.PERCENT_SYMBOL));

            precipitationElm.setText(
                    String.format("%s%s", _m_currentWeather.precipitation, Constants.MILLIMETRES_SYMBOL));

            pressureElm.setText(String.format("%s %s", pressure, Constants.PRESSURE_SYMBOL));

            windSpeedElm.setText(
                    String.format("%s %s", windSpeed,Constants.KILOMETRES_PER_HOUR_SYMBOL));

            directionElm.setText(_m_currentWeather.bearing);

        }

    }

    /*
      Async. task to fetch weather icon
     */
    private class GetWeatherIcon extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0)
        {

            try
            {
                URL url = new URL(String.format(Constants.WEATHER_ICON_UNFORMATTED_URL, _m_currentWeather.icon));
                HttpGet httpRequest;

                httpRequest = new HttpGet(url.toURI());

                HttpClient httpclient = new DefaultHttpClient();
                HttpResponse imgResponse = httpclient
                        .execute(httpRequest);

                HttpEntity entity = imgResponse.getEntity();
                BufferedHttpEntity b_entity = new BufferedHttpEntity(entity);
                InputStream input = b_entity.getContent();

                _m_currentWeather.weatherIcon = BitmapFactory.decodeStream(input);

            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);

            renderView();
        }

        private void renderView()
        {

            ImageView img = (ImageView) getActivity().findViewById(R.id.weather_icon);
            img.setAdjustViewBounds(true);
            img.setMaxHeight(3000);
            img.setMaxWidth(3000);
            img.setImageBitmap(_m_currentWeather.weatherIcon);

        }

    }

    private void loadData()
    {
        // Async. call to fetch data via API call
        new GetTodayWeatherSummary().execute();
    }

}
