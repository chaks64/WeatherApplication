package com.example.weatherapp.ui.main;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.weatherapp.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class FutureForecast extends Fragment {

    private static double latitude;
    private static double longitude;

    public static FutureForecast newInstance(double latitude, double longitude) {
        FutureForecast.latitude = latitude;
        FutureForecast.longitude = longitude;
        return new FutureForecast();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume(){
        super.onResume();
        onViewCreated(getView(), getArguments());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_future_forecast, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        getWeatherInfo(String.valueOf(latitude),String.valueOf(longitude), view);
    }

    protected void getWeatherInfo(String latitude, String longitude, View view) {
        Log.i("ff",latitude+"4"+longitude);
        int[] forecastIds = {R.id.day1fc,R.id.day2fc,R.id.day3fc,R.id.day4fc,R.id.day5fc,R.id.day6fc,R.id.day7fc};
        int[] dayNameIds = {R.id.day1name,R.id.day2name,R.id.day3name,R.id.day4name,R.id.day5name,R.id.day6name,R.id.day7name};
        int[] minTempIds = {R.id.day1mintemp,R.id.day2mintemp,R.id.day3mintemp,R.id.day4mintemp,R.id.day5mintemp,R.id.day6mintemp,R.id.day7mintemp};
        int[] maxTempIds = {R.id.day1maxtemp,R.id.day2maxtemp,R.id.day3maxtemp,R.id.day4maxtemp,R.id.day5maxtemp,R.id.day6maxtemp,R.id.day7maxtemp};
        int[] fciconIds = {R.id.day1fcicon,R.id.day2fcicon,R.id.day3fcicon,R.id.day4fcicon,R.id.day5fcicon,R.id.day6fcicon,R.id.day7fcicon};
        RequestQueue dataQueue = Volley.newRequestQueue(getContext());
        String apiUrl = "https://pro.openweathermap.org/data/2.5/forecast/daily?lat="+latitude+"&lon="+longitude+"&cnt=7&units=imperial&appid=64a7500dbc3ef7aaf81c20dc76bd5741";
        Log.i("getWeatherInfo",apiUrl);
        JsonObjectRequest futureWeatherDataRequest = new JsonObjectRequest( apiUrl, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if(response != null)
                    try {
                        JSONArray data8days = response.getJSONArray("list");
                        for(int i=0;i<data8days.length();i++) {
                            JSONObject day = data8days.getJSONObject(i);
                            Long timestamp = day.getLong("dt");
                            Date date =  new Date(timestamp*1000L);
                            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMM dd");
                            sdf.setTimeZone(TimeZone.getTimeZone("GMT-4"));
                            String currentDate = sdf.format(date);
                            JSONObject temperature = day.getJSONObject("temp");
                            JSONArray weather = day.getJSONArray("weather");
                            JSONObject weatherInfo = weather.getJSONObject(0);
                            String forecastDescription = weatherInfo.getString("description");
                            String forecastMain = weatherInfo.getString("main");
                            int weatherCode = weatherInfo.getInt("id");
                            String dailyMinTemp = "\u2193"+temperature.getString("min")+"\u00B0"+"F";
                            String dailyMaxTemp = "\u2191"+temperature.getString("max")+"\u00B0"+"F";
                            TextView fc = (TextView) view.findViewById(forecastIds[i]);
                            TextView dayName = (TextView) view.findViewById(dayNameIds[i]);
                            TextView minTemp = (TextView) view.findViewById(minTempIds[i]);
                            TextView maxTemp = (TextView) view.findViewById(maxTempIds[i]);
                            ImageView fcicon = (ImageView) view.findViewById(fciconIds[i]);
                            fc.setText(forecastDescription);
                            dayName.setText(currentDate);
                            minTemp.setText(dailyMinTemp);
                            maxTemp.setText(dailyMaxTemp);
                            if( weatherCode == 800)
                                fcicon.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_clear,null));
                            else if( weatherCode<805 && weatherCode>800)
                                fcicon.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_cloudy,null));
                            else if( weatherCode<623 && weatherCode>599)
                                fcicon.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_snow,null));
                            else if( weatherCode<532 && weatherCode>499)
                                fcicon.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_rain,null));
                            else if( weatherCode<322 && weatherCode>299)
                                fcicon.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_drizzle,null));
                            else if( weatherCode<233 && weatherCode>199)
                                fcicon.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_thunderstorm,null));
                            else
                                fcicon.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_atmosphere,null));
                        /*Log.i("Date", currentDate);
                        Log.i("Forecast main", forecastMain);
                        Log.i("Forecast des", forecastDescription);
                        Log.i("Weather code", String.valueOf(weatherCode));
                        Log.i("Daily min", dailyMinTemp);
                        Log.i("Daily max", dailyMaxTemp);*/
                        }
                        JSONObject city = response.getJSONObject("city");
                        String cityName = city.getString("name");
                        String countryName = city.getString("country");
                        TextView city_country = (TextView) view.findViewById(R.id.city);
                        TextView updatedTime = (TextView) view.findViewById(R.id.dateTime);
                        city_country.setText(cityName+" ,"+countryName);
                        SimpleDateFormat dateFormatter = new SimpleDateFormat("MMM dd yyyy HH:mm");
                        Date date = new Date();
                        String dateTimeStr = dateFormatter.format(date);
                        updatedTime.setText("Updated on: "+dateTimeStr);
                        Log.i("Future",city.toString());

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("Failed","errors");
                Log.i("error",error.getMessage());
            }
        });
        dataQueue.add(futureWeatherDataRequest);
    }

}

