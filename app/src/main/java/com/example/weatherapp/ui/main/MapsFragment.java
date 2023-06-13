package com.example.weatherapp.ui.main;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.weatherapp.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class MapsFragment extends Fragment {

    public MapsFragment() {
    }

    SearchView search;
    GoogleMap map;
    TextView current;
    TextView minimum;
    ConstraintLayout weather;
    ImageView fcicon;
    TextView description;

    private OnMapReadyCallback callback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            map = googleMap;
        }


    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        search = view.findViewById(R.id.search);
        current = view.findViewById(R.id.current);
        minimum = view.findViewById(R.id.minimum);
        weather = view.findViewById(R.id.weather);
        fcicon = view.findViewById(R.id.weather_icon);
        description = view.findViewById(R.id.description);

        if(weather.getVisibility() == view.VISIBLE){
            weather.setVisibility(view.GONE);
        }

        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                final Marker[] marker = {null};
                String location = search.getQuery().toString();
                List<Address> addressList = null;
                if(location !=null || location.equals("")){
                    Geocoder geocoder = new Geocoder(getActivity());
                    try {
                        addressList = geocoder.getFromLocationName(location, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Address address = addressList.get(0);
                    final AtomicReference<LatLng>[] latLng = new AtomicReference[]{new AtomicReference<>(new LatLng(address.getLatitude(), address.getLongitude()))};
                    if(marker[0] != null){
                        marker[0].remove();
                    }
                    if(weather.getVisibility() == view.GONE){
                        weather.setVisibility(view.VISIBLE);
                    }
                    search.setQuery(address.getAddressLine(0), false);
                    marker[0] = map.addMarker(new MarkerOptions().position(latLng[0].get()).title(location));
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng[0].get(), 10));

                    if(map != null){
                        map.setOnMapClickListener(point -> {
                            try {
                                latLng[0].set(point);
                                List<Address> pickList = geocoder.getFromLocation(point.latitude, point.longitude, 1);
                                String city = pickList.get(0).getAddressLine(0);
                                search.setQuery(city, false);
                                if(marker[0] != null){
                                    marker[0].remove();
                                }
                                getLocalWeather1(latLng[0].get().latitude, latLng[0].get().longitude, view);
                                marker[0] = map.addMarker(new MarkerOptions().position(point).title(location));
                                map.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 10));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                    }

                    Log.i("LatLng", "onQueryTextSubmit: "+latLng[0].get().latitude);

                    getLocalWeather1(latLng[0].get().latitude, latLng[0].get().longitude, view);



                }

                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }

    private void getLocalWeather1(double latitude, double longitude, View view) {
        RequestQueue dataQueue = Volley.newRequestQueue(getContext());
        String apiUrl = "https://api.openweathermap.org/data/2.5/weather?lat="+latitude+"&lon="+longitude+"&appid=3420ebdc51dcf444e50ca658c33909f3&units=imperial";
        Log.i("getWeatherInfo", String.valueOf(latitude));
        try{
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(apiUrl, response -> {
                if (response != null)
                    Log.i("getLocalWeather", "Success");
                try {
                    JSONArray currentWeatherCondition = response.getJSONArray("weather");
                    JSONObject weather = response.getJSONObject("main");
                    JSONObject weatherInfo = currentWeatherCondition.getJSONObject(0);
                    int weatherCode = weatherInfo.getInt(("id"));

                    Log.i("TAG", "getLocalWeather: "+weatherInfo);
                    current.setText("Current: "+weather.getString("temp")+"\u00B0"+"F");
                    minimum.setText("Minimum: "+weather.getString("temp_min")+"\u00B0"+"F");
                    description.setText(weatherInfo.getString("description"));

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
}