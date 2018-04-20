package com.example.jett.milemarker;

import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;

/**
 * Interprets the address for interpolating the roads when taking trips from Google.
 */
public class snapToRoads extends AsyncTask<Void, Void, Void> {
    static final String baseURL = "https://roads.googleapis.com/v1/snapToRoads?path=";
    private String key = "";
    GoogleMap map;
    PolylineOptions polyline;

    snapToRoads(String accessKey) {
        key = accessKey;
    }

    ArrayList<Location> locations = new ArrayList<>();

    ArrayList<JSONresponse.snappedPoints> getSnapPoints() {
        String snapURL = baseURL;
        ArrayList<JSONresponse.snappedPoints> coordinates = new ArrayList<JSONresponse.snappedPoints>();

        for (int i = 0; i < locations.size();i++){
            snapURL += locations.get(i).getLatitude() + "," + locations.get(i).getLongitude();
            if (i < locations.size() - 1){
                snapURL += "|";
            }
        }
        snapURL += "&interpolate=true&key=" + key;
        try{
            Log.d("snapToRoads",snapURL);
            InputStream stream = new URL(snapURL).openStream();
            Reader reader = new InputStreamReader(stream);
            JSONresponse response = new Gson().fromJson(reader, JSONresponse.class);
            stream.close();
            coordinates = new ArrayList<>(response.snappedPoints);
        }
        catch (IOException e){
            Log.d("snapToRoads","No URL to follow.");
        }
        return coordinates;
    } //Coordinates are returned here as points to snap to the road.

    /**
     * Makes the snap to roads work in the background and adds polylines as it does so.
     * @param voids
     * @return null
     */
    @Override
    protected Void doInBackground(Void... voids) {
        ArrayList<JSONresponse.snappedPoints> snappedLocations = getSnapPoints();
        PolylineOptions polyline = new PolylineOptions().color(Color.RED).width(5);

        for(int i = 0;i < snappedLocations.size();i++){
            polyline.add(new LatLng(snappedLocations.get(i).location.latitude, snappedLocations.get(i).location.longitude));
        }

        this.polyline = polyline;
        return null;
    }

    /**
     * Displays the polylines created in the do in background.
     * Makes sure longitudes and latitudes are correctly placed inside arraylist.
     * @param aVoid
     */
    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        map.addPolyline(polyline);
    }

    ArrayList<LongLatSerial> getSerialArrayList() {
        ArrayList<LongLatSerial> returning = new ArrayList<>();
        for (Location i : locations) {
            if (i != null) {
                returning.add(new LongLatSerial(i));
            }
        }
        return returning;
    }
}

class LongLatSerial implements Serializable {
    Double longitude;
    Double latitude;

    LongLatSerial (Location input) {
        longitude = input.getLongitude();
        latitude = input.getLatitude();
    }

}
