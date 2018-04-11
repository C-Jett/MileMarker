package com.example.jett.milemarker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;



public class Main extends AppCompatActivity implements OnMapReadyCallback, android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback {

    private SharedPreferences preferences;

    private FusedLocationProviderClient locationServices;
    private GoogleMap gMap;
    private snapToRoads snapToRoads;

    private Button findMe;
    private Button trackMe;

    private boolean trackingOn = false;
    private Timer trackingTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        preferences = getPreferences(MODE_PRIVATE);
        snapToRoads = new snapToRoads(this.getString(R.string.google_maps_key));

        locationServices = LocationServices.getFusedLocationProviderClient(this);
        SupportMapFragment mappie = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mappie);
        mappie.getMapAsync(this);

        findMe = (Button) this.findViewById(R.id.findButton);
        findMe.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                if (!getPermissionsGranted(new String[]{Manifest.permission.ACCESS_FINE_LOCATION})){
                    askPermission();
                }
                else {
                    gMap.setMyLocationEnabled(true);
                    getDeviceLocation(false, "");
                }
            }
        });

        trackMe = this.findViewById(R.id.trackButton);
        trackMe.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                if (!trackingOn) {
                    if (!getPermissionsGranted(new String[]{Manifest.permission.ACCESS_FINE_LOCATION})) {
                        askPermission();
                    }
                    else {
                        trackingOn = true;
                        gMap.setMyLocationEnabled(true);
                        trackMe.setText("Turn off tracking");
                        timerControl(true);
                    }
                }
                else {
                    trackMe.setText("Track Me");
                    timerControl(false);
                }
            }
        });
    }

    void timerControl(Boolean isTimerOn){

        if (isTimerOn){
            getDeviceLocation(true, "Start");
            trackingTimer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    getDeviceLocation(false, "");
                }
            };
            trackingTimer.scheduleAtFixedRate(task, 0, 20000);
        }
        else {
            getDeviceLocation(true, "Finish");
            trackingTimer.cancel();
            snapToRoads.execute();
            try{
                SharedPreferences.Editor editor = preferences.edit();
                ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
                ObjectOutputStream output = new ObjectOutputStream(byteArray);
                output.writeObject(snapToRoads.locations);
                output.close();
                editor.putString("Trip", byteArray.toString());
            }
            catch (IOException e){
                e.printStackTrace();
                return;
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        snapToRoads.map = gMap;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (getPermissionsGranted(permissions)) {
            Toast.makeText(this, "Permissions Granted", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this, "Permissions Denied", Toast.LENGTH_SHORT).show();
        }
    }

    void askPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1039);
        }
    }

    boolean getPermissionsGranted(String[] permissions){
        for (String permission: permissions){
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }

    @SuppressLint("MissingPermission")
    void getDeviceLocation(final Boolean addPin, final String pinTitle){
        locationServices.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()){
                    Location result = task.getResult();

                    snapToRoads.locations.add(result);
                    gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(result.getLatitude(), result.getLongitude()), 15));
                    if (addPin){
                        gMap.addMarker(new MarkerOptions().position(new LatLng(result.getLatitude(), result.getLongitude()))
                                .title(pinTitle));
                    }
                }
            }
        });
    }
}
