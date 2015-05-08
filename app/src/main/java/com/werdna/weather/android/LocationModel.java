package com.werdna.weather.android;


import android.app.Activity;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import java.io.IOException;
import java.util.List;

/*
 Class for storing the user's city, country and country code
 Author: Andrew Buchan
 Creation Date: 2015-05-06
 Modified Date: 2015-05-06
 */
public class LocationModel {

    String city;
    String countryCode;
    String country;
    boolean retrievedAddress = false;

    public LocationModel GetLocation(Activity callingActivity)
    {
        LocationModel locModel = new LocationModel();
        double deviceLatitude = 0;
        double deviceLongitude = 0;

        GPSUtility gps = new GPSUtility(callingActivity);
        if(gps.canGetLocation())
        {
            deviceLatitude = gps.latitude;
            deviceLongitude = gps.longitude;
        }
        gps.stopUsingGPS();

        try
        {
            List<Address> addresses = new Geocoder(callingActivity).getFromLocation(deviceLatitude, deviceLongitude, 1);

            if(addresses != null)
            {
                Address returnedAddress = addresses.get(0);
                String cityName = returnedAddress.getLocality();
                String country = returnedAddress.getCountryName();
                String countryCode = returnedAddress.getCountryCode();
                locModel.city = cityName;
                locModel.country = country;
                locModel.countryCode = countryCode;
                locModel.retrievedAddress = true;
            }
            else
            {
                Log.d("Error", "No address returned.");
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return locModel;
    }

}
