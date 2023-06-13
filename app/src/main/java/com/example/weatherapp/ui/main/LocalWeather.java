package com.example.weatherapp.ui.main;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.weatherapp.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;


public class LocalWeather extends Fragment {
    private static double latitude;
    private static double longitude;

    public LocalWeather() {
        // Required empty public constructor
    }

    public static LocalWeather newInstance(double latitude, double longitude) {
        LocalWeather.latitude = latitude;
        LocalWeather.longitude = longitude;
        return new LocalWeather();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_local_weather, container,false);

        //Code to swipe down and refresh
        final SwipeRefreshLayout pullToRefresh = view.findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(() -> {
            System.out.println("refreshing");
            onViewCreated(view, savedInstanceState);
            pullToRefresh.setRefreshing(false);
        });
        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        //Show progress bar when no data fetched
        ProgressBar progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        //Hide all UI elements
        TextView cityName = view.findViewById(R.id.cityName);
        cityName.setVisibility(View.GONE);

        TextView date = view.findViewById(R.id.dateTime);
        date.setVisibility(View.GONE);

        ImageView weatherConditionImg = view.findViewById(R.id.weatherConditionImg);
        weatherConditionImg.setVisibility(View.GONE);

        TextView weatherConditionText = view.findViewById(R.id.weatherConditionText);
        weatherConditionText.setVisibility(View.GONE);

        TextView temperature = view.findViewById(R.id.temperature);
        temperature.setTextColor(View.GONE);

        TextView feelsLike = view.findViewById(R.id.feelsLike);
        feelsLike.setVisibility(View.GONE);

        TextView visibilityView = view.findViewById(R.id.visibility);
        visibilityView.setVisibility(View.GONE);

        getLocalWeather(latitude, longitude, view);
    }

    protected void getLocalWeather(double latitude, double longitude, View view) {
        RequestQueue dataQueue = Volley.newRequestQueue(getContext());
        String apiUrl = "https://pro.openweathermap.org/data/2.5/weather?lat=" + latitude + "&lon=" + longitude + "&units=imperial&appid=64a7500dbc3ef7aaf81c20dc76bd5741";
        Log.i("getWeatherInfo", apiUrl);
        try{
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(apiUrl, response -> {
                if (response != null) {
                    Log.i("getLocalWeather", "Success");
                }
                try {
                    String city = response.getString("name");
                    JSONArray currentWeatherCondition = response.getJSONArray("weather");
                    JSONObject weather = response.getJSONObject("main");
                    int visibility = response.getInt("visibility");
                    long dateTime = response.getLong("dt")*1000;
                    SimpleDateFormat dateFormatter = new SimpleDateFormat("MMM dd yyyy HH:mm");
                    //Parsing the given String to Date object
                    Date date = new Date(dateTime);
                    String dateTimeStr = dateFormatter.format(date);

                    setValues(view, city, dateTimeStr, weather, currentWeatherCondition, visibility*0.00062137);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, error -> {
                Log.i("Failed", "errors");
                Log.i("error", error.getMessage());
            });
            dataQueue.add(jsonObjectRequest);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    protected void setValues(View view, String city, String dateTime, JSONObject weather, JSONArray currentWeatherCondition, double visibility) throws JSONException {
        //Hide progress bar when data fetched successfully
        ProgressBar progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        //City
        TextView cityName = view.findViewById(R.id.cityName);
        cityName.setText(city);
        cityName.setTextColor(Color.parseColor("#494544"));
        cityName.setVisibility(View.VISIBLE);

        //Date and time
        TextView date = view.findViewById(R.id.dateTime);
        date.setText("Last updated: " + dateTime);
        date.setVisibility(View.VISIBLE);

        //Weather condition image
        JSONObject weatherCondition = currentWeatherCondition.getJSONObject(0);
        String iconCode = weatherCondition.get("icon").toString();
        String imageURL  = "http://openweathermap.org/img/wn/" + iconCode + "@2x.png";
        String description = "The weather condition is " + weatherCondition.get("description");
        System.out.println("image url: " + imageURL);
        ImageView weatherConditionImg = view.findViewById(R.id.weatherConditionImg);
        //Picasso.get().setLoggingEnabled(true);
        Picasso.get().load(imageURL).into(weatherConditionImg);
        weatherConditionImg.setVisibility(View.VISIBLE);

        //Weather condition description
        TextView weatherConditionText = view.findViewById(R.id.weatherConditionText);
        weatherConditionText.setText(description);
        weatherConditionText.setTextColor(Color.parseColor("#494544"));
        weatherConditionText.setVisibility(View.VISIBLE);

        //Temperature
        String temperatureStr = weather.get("temp") + "\u00B0 F";
        TextView temperature = view.findViewById(R.id.temperature);
        temperature.setText(temperatureStr);
        temperature.setTextColor(Color.parseColor("#494544"));
        temperature.setVisibility(View.VISIBLE);

        //Feels like temperature
        String feelsLikeStr = "Feels like " + weather.get("feels_like") + "\u00B0 F";
        TextView feelsLike = view.findViewById(R.id.feelsLike);
        feelsLike.setText(feelsLikeStr);
        feelsLike.setTextColor(Color.parseColor("#494544"));
        feelsLike.setVisibility(View.VISIBLE);

        //Visibility
        TextView visibilityView = view.findViewById(R.id.visibility);
        visibilityView.setText("The visibility is " + String.format("%.2f", visibility) + "mi");
        visibilityView.setTextColor(Color.parseColor("#494544"));
        visibilityView.setVisibility(View.VISIBLE);
    }
}