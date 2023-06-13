package com.example.weatherapp;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;

import com.example.weatherapp.databinding.ActivityMainBinding;
import com.example.weatherapp.ui.main.SectionsPagerAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.google.android.material.tabs.TabLayout;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks, LocationListener {

    private ActivityMainBinding binding;
    private final int LOCATION_CONSENT = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private int location_deny_count = 0;
    private int location_dialog_cancel_count = 0;
    private int gps_dialog_cancel_count = 0;
    private int gps_deny_count = 0;

    private double latitude;
    private double longitude;

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*handle user consent for accessing the device location
         * code reference from https://github.com/googlesamples/easypermissions*/
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        /*when the app is opened check for gps*/
        requestGPS();
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public boolean checkGPS() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean is_location_and_provider_enabled = false;
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            is_location_and_provider_enabled = true;
        }
        return is_location_and_provider_enabled;
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

    }

    @Override
    public void onLocationChanged(@NonNull List<Location> locations) {
        LocationListener.super.onLocationChanged(locations);
    }

    @Override
    public void onFlushComplete(int requestCode) {
        LocationListener.super.onFlushComplete(requestCode);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        LocationListener.super.onProviderEnabled(provider);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        LocationListener.super.onProviderDisabled(provider);
    }

    public void requestUserConsent() {
        String[] perms = {Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.INTERNET};
        if (EasyPermissions.hasPermissions(this, perms)) {
            // Already have permission, do the thing
            currentInfo();
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, getString(R.string.location_rationale),
                    LOCATION_CONSENT, perms);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onResume() {
        /*onResume will be called when user comes back to app from other apps such as settings or permission request dialogs*/
        super.onResume();
        /*check for gps because user might have turned off while switching between apps */
        if(checkGPS()){
            /*gps is truned on, check for location access
            * 3 scenarios to consider when on resume is called after user comes back from app permission dialog
            * one is he has granted permission and not*/
            String[] perms = {Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.INTERNET};
            /*scenario 1 - user granted permission for app to access location so, show weather info for current location*/
            if (EasyPermissions.hasPermissions(this, perms)) {
                // Already have permission, do the thing
                Log.i("onResume-if","has permission - current info");
                currentInfo();

            }
            /*scenario 2- user was not asked permission*/
            else if(location_deny_count == 0){
                requestUserConsent();
            }
            /*scenario 3 - user did not grant permission for app to access location so, show weather info for default location*/
            else {
                Toast.makeText(this, "Location Access denied, showing weather for Milpitas, US", Toast.LENGTH_SHORT)
                        .show();
                Log.i("onResume-else","no permission - default info");
                defaultInfo();

            }
        }
        /*if gps is not enabled when user comes back to app means he did not turned on gps through custom dialog in requestGPS() function. so show him
        * default info assuming that he is not willing to turn on gps*/
        else{
            gps_deny_count++;
             if(gps_deny_count>0 && gps_dialog_cancel_count>0) {
                Toast.makeText(this, "GPS disabled, showing weather for Milpitas, US", Toast.LENGTH_SHORT)
                        .show();
                defaultInfo();
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        fusedLocationClient = null;
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onRestart() {
        super.onRestart();
        if (checkGPS()) {
            String[] perms = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.INTERNET};
            if (EasyPermissions.hasPermissions(this, perms)) {
                // Already have permission, do the thing
                Log.i("onRestart-if","has permission - current info");
                currentInfo();
            }
            else if(location_deny_count == 0){
                requestUserConsent();
            }
            else {
                Toast.makeText(this, "Location Access denied, showing weather for Milpitas, US", Toast.LENGTH_SHORT)
                        .show();
                Log.i("onRestart-else","no permission - default info");
                defaultInfo();
            }
        } else {
            gps_deny_count++;
            if(gps_dialog_cancel_count==0)
                requestGPS();
            else if(gps_deny_count>0 && gps_dialog_cancel_count>0) {
                Toast.makeText(this, "GPS disabled, showing weather for Milpitas, US", Toast.LENGTH_SHORT)
                        .show();
                defaultInfo();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        fusedLocationClient = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.finishActivity(0);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        Log.i("Permission granted", "Request code: "+requestCode+": "+perms);
        currentInfo();
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Log.d("onPermissionDenied", "onPermissionsDenied:" + requestCode + ":" + perms.size());
        location_deny_count++;
        Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_LONG)
                .show();
        defaultInfo();
    }


    public void defaultInfo(){
        /*weather data for default location milpitas when user denies to grant location permission*/
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager(),37.432335,-121.899574);
        ViewPager viewPager = binding.viewPager;
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = binding.tabs;
        tabs.setupWithViewPager(viewPager);
    }

    public void currentInfo(){

        // Already have permission, do the thing
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        /*code reference for current location
         * https://developer.android.com/training/location/retrieve-current*/
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, new CancellationToken() {
                    @NonNull
                    @Override
                    public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
                        return null;
                    }

                    @Override
                    public boolean isCancellationRequested() {
                        return false;
                    }
                })
                .addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location currentLocation) {
                        // Got last known location. In some rare situations this can be null.
                        if (currentLocation != null) {
                            latitude = currentLocation.getLatitude();
                            longitude = currentLocation.getLongitude();
                            Log.i("Location details", latitude + " " +longitude );
                            SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(MainActivity.this, getSupportFragmentManager(),latitude,longitude);
                            ViewPager viewPager = binding.viewPager;
                            viewPager.setAdapter(sectionsPagerAdapter);
                            TabLayout tabs = binding.tabs;
                            tabs.setupWithViewPager(viewPager);
                        }
                    }
                });
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public void requestGPS(){
        if (checkGPS()) {
            /*if gps is already enabled check if the app has location access*/
            requestUserConsent();
        }
        else {
            /*if gps is not on, ask user to on it it through a custom dialog*/
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Please enable GPS");
            builder.setMessage("Weather app needs GPS to find location");
            // Add the buttons
            builder.setPositiveButton(R.string.open_settings, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked OK button, take them to gps settings
                    final Intent gps_intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(gps_intent);
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog, so show a message and keep track of user cancelled the open gps settings dialog
                    // this means user do not want to on gps, after cancel onResume() will be called
                    gps_dialog_cancel_count++;
                    Toast.makeText(MainActivity.this, "Please enable the GPS for this app to work", Toast.LENGTH_SHORT).show();
                }
            });
            // Set other dialog properties
            // Create the AlertDialog
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

}