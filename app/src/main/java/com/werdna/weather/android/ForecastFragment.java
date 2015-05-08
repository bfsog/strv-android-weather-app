package com.werdna.weather.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/*
  Activity fragment to display  n-day forecast for location retrieved by location services
  Author: Andrew Buchan
  Creation Date: 2015-05-06
  Modified Date: 2015-05-06
 */
public class ForecastFragment extends Fragment {

    // Class instance variables
    private List<WeatherModel> _m_forecastWeather;
    private LocationModel _m_usersLocation;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_forecast, container, false);

        _m_usersLocation = new LocationModel().GetLocation(this.getActivity());
        _m_forecastWeather = new ArrayList<>();

        new GetForecastWeatherSummary().execute();

        return rootView;
    }

    /*
      Async. task to call OpenWeather API and get forecast
     */
    private class GetForecastWeatherSummary extends AsyncTask<Void, Void, Void>
    {

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }

        private String GetForecastResponse()
        {
            String response = "";
            WebRequest webRequest = new WebRequest();

            try
            {
                response = webRequest.call(
                        String.format(Constants.FORECAST_WEATHER_UNFORMATTED_URL,
                                _m_usersLocation.city,
                                _m_usersLocation.countryCode,
                                Constants.FORECAST_NUMBER_OF_DAYS),
                        WebRequest.GET);
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }

            return response;
        }

        private long GetFutureDateAsLong()
        {
            long epoch;

            Date currentDate = new Date();

            Calendar cal = Calendar.getInstance();
            cal.setTime(currentDate);
            cal.add(Calendar.MINUTE, 10);
            currentDate = cal.getTime();
            epoch = currentDate.getTime();

            return epoch;
        }

        @Override
        protected Void doInBackground(Void... arg0)
        {

            String response = "";
            try
            {

                // Only get response from server if cache does not exist, or if it exists and is
                // older than 10 minutes
                final SharedPreferences pref = getActivity().getApplicationContext().getSharedPreferences(Constants.PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
                final SharedPreferences.Editor editor = pref.edit();
                boolean forecastResponseExists = pref.getBoolean(Constants.PREFERENCES_FORECAST_RESPONSE_EXISTS_KEY, false);
                if(forecastResponseExists)
                {
                    Date currentDate = new Date();
                    long currentDateInMS = currentDate.getTime();
                    long forecastRefreshDateInMS = pref.getLong(Constants.PREFERENCES_FORECAST_DATE_REFRESH_IN_MS_KEY, 0);
                    if(currentDateInMS > forecastRefreshDateInMS)
                    {
                        response = GetForecastResponse();
                        long epoch = GetFutureDateAsLong();
                        editor.putLong(Constants.PREFERENCES_FORECAST_DATE_REFRESH_IN_MS_KEY, epoch);
                        editor.apply();
                    }
                    else
                    {
                        response = pref.getString(Constants.PREFERENCES_FORECAST_RESPONSE_KEY, "");
                    }
                }
                else
                {
                    long epoch = GetFutureDateAsLong();
                    response = GetForecastResponse();
                    editor.putBoolean(Constants.PREFERENCES_FORECAST_RESPONSE_EXISTS_KEY, true);
                    editor.putString(Constants.PREFERENCES_FORECAST_RESPONSE_KEY, response);
                    editor.putLong(Constants.PREFERENCES_FORECAST_DATE_REFRESH_IN_MS_KEY, epoch);
                    editor.apply();
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            if(response != null)
            {
                try
                {
                    JSONObject forecastObj = new JSONObject(response);
                    JSONArray forecasts = forecastObj.getJSONArray("list");
                    Date currentDate = new Date();
                    for(int forecastIndex = 0; forecastIndex < forecasts.length(); forecastIndex++)
                    {

                        JSONObject thisForecast = forecasts.getJSONObject(forecastIndex);

                        JSONArray weatherArr = thisForecast.getJSONArray("weather");
                        JSONObject weather = weatherArr.getJSONObject(0);

                        // add a day to currentDate so the next day is set for the forecast UI
                        // This is used to populate the correct week day
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(currentDate);
                        String dayOfWeek = new SimpleDateFormat("EEEE", Locale.CANADA).format(currentDate);
                        cal.add(Calendar.DATE, 1);
                        currentDate = cal.getTime();

                        String summary = weather.getString("main");
                        String icon = weather.getString("icon");

                        JSONObject tempObj = thisForecast.getJSONObject("temp");
                        Double temp = tempObj.getDouble("day");

                        WeatherModel newForecast = new WeatherModel();
                        newForecast.dayOfWeek = dayOfWeek;
                        newForecast.summary = summary;
                        newForecast.icon = icon;
                        newForecast.temperature = temp;

                        // At this stage we should get the image from the server
                        try
                        {
                            URL url = new URL(String.format(Constants.WEATHER_ICON_UNFORMATTED_URL, newForecast.icon));
                            HttpGet httpRequest;

                            httpRequest = new HttpGet(url.toURI());

                            HttpClient httpclient = new DefaultHttpClient();
                            HttpResponse imgResponse = httpclient.execute(httpRequest);

                            HttpEntity entity = imgResponse.getEntity();
                            BufferedHttpEntity b_entity = new BufferedHttpEntity(entity);
                            InputStream input = b_entity.getContent();

                            newForecast.weatherIcon = BitmapFactory.decodeStream(input);
                        }
                        catch (Exception ex)
                        {
                            ex.printStackTrace();
                        }

                        _m_forecastWeather.add(newForecast);

                    }

                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);

            ArrayAdapter<WeatherModel> weatherAdapter = new ArrayAdapter<WeatherModel>(getActivity(), R.layout.forecast_list_item, R.id.weekday, _m_forecastWeather) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);

                    // Reference UI fields for display
                    TextView weekDayElm = (TextView)view.findViewById(R.id.weekday);
                    TextView temperatureElm = (TextView)view.findViewById(R.id.temperature);
                    TextView descriptionElm = (TextView)view.findViewById(R.id.description);
                    ImageView weatherIconElm = (ImageView)view.findViewById(R.id.weather_icon);

                    // Output values to UI
                    weekDayElm.setText(getItem(position).dayOfWeek);
                    int temperature = (int)Math.round(getItem(position).temperature);
                    temperatureElm.setText(String.valueOf(temperature) + Constants.DEGREE_SYMBOL);
                    descriptionElm.setText(getItem(position).summary);
                    weatherIconElm.setAdjustViewBounds(true);
                    weatherIconElm.setMaxHeight(3000);
                    weatherIconElm.setMaxWidth(3000);
                    weatherIconElm.setImageBitmap(getItem(position).weatherIcon);
                    return view;
                }
            };

            ListView forecastList = (ListView)getActivity().findViewById(R.id.forecasstList);
            forecastList.setAdapter(weatherAdapter);

        }

    }

}
