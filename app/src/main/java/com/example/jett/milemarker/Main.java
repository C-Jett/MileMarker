package com.example.jett.milemarker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.media.Image;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


/**
* Class: Main
* Description: Main view that displays gMap on screen with two track buttons. This is the main trip
*              view and also has a stop screen to keep you from watching your phone while driving.
 **/
public class Main extends AppCompatActivity implements OnMapReadyCallback, android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback {

    private SharedPreferences preferences;
    private Integer historyIndexToPull;
    private FusedLocationProviderClient locationServices;
    private GoogleMap gMap;
    private snapToRoads snapToRoads;

    private TextView doNotText;
    private View viewFragment;
    private ImageButton ImageButton;
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

        //Accesses our keys and other important information for connecting with the info client.
        locationServices = LocationServices.getFusedLocationProviderClient(this);
        SupportMapFragment mappie = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mappie);
        mappie.getMapAsync(this);

        ImageButton = findViewById(R.id.imageButton);
        viewFragment = findViewById(R.id.viewFragment);
        doNotText = findViewById(R.id.doNotText);

        findMe = (Button) this.findViewById(R.id.findButton);
        findMe.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                if (!getPermissionsGranted(new String[]{Manifest.permission.ACCESS_FINE_LOCATION})) {
                    askPermission();
                } else {
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
                    } else {
                        trackingOn = true;
                        gMap.setMyLocationEnabled(true);
                        viewFragment.setVisibility(View.VISIBLE);
                        ImageButton.setVisibility(View.VISIBLE);
                        findMe.setVisibility(View.INVISIBLE);
                        trackMe.setVisibility(View.INVISIBLE);
                        doNotText.setVisibility(View.VISIBLE);
                        timerControl(true);
                    }
                }
            }
        });

        ImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewFragment.setVisibility(View.INVISIBLE);
                ImageButton.setVisibility(View.INVISIBLE);
                findMe.setVisibility(View.VISIBLE);
                trackMe.setVisibility(View.VISIBLE);
                doNotText.setVisibility(View.INVISIBLE);
                trackMe.setText("Track Me");
                timerControl(false);
                Intent intent = new Intent(getApplicationContext(), startScreen.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Controls the time in which locations are discovered.
     * Default 4 seconds (listed in milliseconds)
     **/
    void timerControl(Boolean isTimerOn) {
        if (isTimerOn) {
            getDeviceLocation(true, "Start");
            trackingTimer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    getDeviceLocation(false, "");
                }
            };
            trackingTimer.scheduleAtFixedRate(task, 0, 4000);
        } else {
            getDeviceLocation(true, "Finish");
            trackMe.setEnabled(false);
            trackingTimer.cancel();
            snapToRoads.execute();

            File directory = getFilesDir();
            File historyFile = new File(directory, "history");

            try {
                if (historyFile.exists()) {
                    ArrayList<ArrayList<LongLatSerial>> history = null;
                    FileInputStream input = new FileInputStream(historyFile);
                    ObjectInputStream objIn = new ObjectInputStream(input);
                    Object historyObject = objIn.readObject();
                    objIn.close();
                    if (historyObject instanceof ArrayList) {
                        history = (ArrayList<ArrayList<LongLatSerial>>) historyObject;
                    } else {
                        Log.d("INSTANCEUPDATE", "Object for history list is incorrect");
                        return;
                    }
                    if (history != null) {
                        history.add(snapToRoads.getSerialArrayList());
                        FileOutputStream fileOut = new FileOutputStream(historyFile);
                        ObjectOutputStream objOut = new ObjectOutputStream(fileOut);
                        objOut.writeObject(history);
                        objOut.close();
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } //Long and lats are now written to file and the stream from serialization is finished.
        } //Map is saved with starting and ending locations.
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        snapToRoads.map = gMap;
    }

    /**
     * For granting location permissions.
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (getPermissionsGranted(permissions)) {
            Toast.makeText(this, "Permissions Granted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Permissions Denied", Toast.LENGTH_SHORT).show();
        }
    }

    void askPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1039);
        }
    }

    boolean getPermissionsGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets device locations at the start and allows the camera to center over the gMap view.
     * @param addPin - whether or not to add pin
     * @param pinTitle - Name of the pin being placed
     */
    @SuppressLint("MissingPermission")
    void getDeviceLocation(final Boolean addPin, final String pinTitle) {
        locationServices.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    Location result = task.getResult();
                    snapToRoads.locations.add(result);
                    gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(result.getLatitude(), result.getLongitude()), 15));
                    if (addPin) {
                        gMap.addMarker(new MarkerOptions().position(new LatLng(result.getLatitude(), result.getLongitude()))
                                .title(pinTitle));
                    }
                }
            }
        });
    }

}