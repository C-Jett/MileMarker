package com.example.jett.milemarker;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class trip_info extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap gMap;
    private Integer historyIndexToPull;
    private TextView distance;
    private TextView startPoint;
    private TextView endPoint;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_info);

        distance = findViewById(R.id.distance);
        startPoint = findViewById(R.id.startPoint);
        endPoint = findViewById(R.id.endPoint);

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
                deleteSelectedTrip();
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

                        distanceCalculator distanceCalculator = new distanceCalculator(historyToPull, this.getString(R.string.google_maps_key),distance,startPoint,endPoint);
                        distanceCalculator.execute();

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

    void deleteSelectedTrip() {
        ArrayList<ArrayList<LongLatSerial>> historyArray;
        historyArray = getHistoryArray();
        if(historyArray != null){
           historyArray.remove(historyIndexToPull.intValue());
            historyToFile(historyArray);
        }
        else {
            Log.d("IT WAS NULL", "NULLLLLLLLLLLLLLLLLLLLLLLLLL");
        }
    }

    ArrayList<ArrayList<LongLatSerial>> getHistoryArray() {
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
                    return history;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    void historyToFile(ArrayList<ArrayList<LongLatSerial>> historyToFile){
       try {
           File directory = getFilesDir();
           File historyFile = new File(directory, "history");
           FileOutputStream fileOut = new FileOutputStream(historyFile);
           ObjectOutputStream objOut = new ObjectOutputStream(fileOut);
           objOut.writeObject(historyToFile);
           objOut.close();
       }
       catch (IOException e){
           e.printStackTrace();
       }
    }

    void updateDistance(String toSet) {
        distance.setText(toSet);
    }
}
