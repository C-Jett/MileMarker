package com.example.jett.milemarker;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

public class trip_info extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap gMap;
    private Integer historyIndexToPull;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_info);


        SupportMapFragment mappie = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapView);
        mappie.getMapAsync(this);

        if (getIntent().getExtras() != null && getIntent().getExtras().containsKey("historyIndex")) {
            Integer historyIndex = getIntent().getExtras().getInt("historyIndex");
            historyIndexToPull = historyIndex;
        }

        Button deleteTrip = findViewById(R.id.deleteTripButton);
        deleteTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deletePolyLineFromHistory();
                Intent intent = new Intent(getApplicationContext(), startScreen.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        if (historyIndexToPull != null) {
            loadPolyLineFromHistory();
        }
    }

    void loadPolyLineFromHistory() {
        if (gMap != null) {
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
                        ArrayList<LongLatSerial> historyToPull = history.get(historyIndexToPull);

                        PolylineOptions lineToDraw = new PolylineOptions().color(Color.RED).width(5);
                        for (LongLatSerial i : historyToPull) {
                            lineToDraw.add(new LatLng(i.latitude, i.longitude));
                        }
                        gMap.addPolyline(lineToDraw);
                        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lineToDraw.getPoints().get(0), 15));
                        gMap.addMarker(new MarkerOptions().position(new LatLng(lineToDraw.getPoints().get(0).latitude, lineToDraw.getPoints().get(0).longitude)).title("Start"));
                        gMap.addMarker(new MarkerOptions().position(new LatLng(lineToDraw.getPoints().get(lineToDraw.getPoints().size() - 1).latitude, lineToDraw.getPoints().get(lineToDraw.getPoints().size() - 1).longitude)).title("Finish"));
                    } else {
                        Log.d("INSTANCEUPDATE", "Object for history list is incorrect");
                        return;
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    void deletePolyLineFromHistory() {
        if (gMap != null) {
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
                        ArrayList<LongLatSerial> historyToPull = history.get(historyIndexToPull);
                        historyFile.delete();

                    } else {
                        Log.d("INSTANCEUPDATE", "Object for history list is incorrect");
                        return;
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
