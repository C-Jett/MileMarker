package com.example.jett.milemarker;

import android.os.AsyncTask;
import android.widget.TextView;

import com.google.gson.Gson;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Asynchronous calculation of distance using our LongLatSerial array.
 */
public class distanceCalculator extends AsyncTask<Void, Void, Void> {
    private String key = "";
    private TextView toUpdateDistance;
    private TextView toUpdateStart;
    private TextView toUpdateFinish;
    private DistanceResponse response = null;
    private ArrayList<LongLatSerial> longLatDistance;

    distanceCalculator(ArrayList<LongLatSerial> longLatDistance, String key, TextView toUpdateDistance, TextView toUpdateStart, TextView toUpdateFinish){
        this.longLatDistance = longLatDistance;
        this.key = key;
        this.toUpdateDistance = toUpdateDistance;
        this.toUpdateStart = toUpdateStart;
        this.toUpdateFinish = toUpdateFinish;
    }

    /**
     * Places information within the key needed for Google to return the locations.
     * @return (String) returningURL
     */
    private String distanceURL(){
        final String baseURL = "https://maps.googleapis.com/maps/api/distancematrix/json?units=imperial&origins=";
        String returningURL = baseURL;
        for(int i = 0; i < longLatDistance.size();i++){
            if(longLatDistance.get(i) != null){
                if(i < longLatDistance.size()-2){
                    returningURL += "" + longLatDistance.get(i).latitude + "," + longLatDistance.get(i).longitude;
                    returningURL += "|";
                }
                else{
                    returningURL += "&destinations=" + longLatDistance.get(i).latitude + "," + longLatDistance.get(i).longitude;
                }
            }
        }
        returningURL += "&key=" + key;
        return returningURL;
    }

    /**
     * Performs tasks in the background and then closes the stream from serialized data.
     * @param voids
     * @return null
     */
    @Override
    protected Void doInBackground(Void... voids) {
        String stringURL = distanceURL();
        try {
            URL url = new URL(stringURL);
            Reader reader = new InputStreamReader(url.openStream());
            response = new Gson().fromJson(reader, DistanceResponse.class);
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Upon completing the backgroudn tasks this will set the text on other views.
     * Definitely better ways to do this but could not get <WeakReference> to work.
     * @param aVoid
     */
    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        Double total = 0.0;
        String finalDistance = "";
        String startLocation =  "";
        String endLocation = "";
        for (int i = 0; i < response.rows.size(); i++) {
            DistanceResponse.element current = response.rows.get(i).elements.get(0);
            startLocation = response.origin_addresses.get(0);
            endLocation = response.origin_addresses.get(i);
            String[] stringArray = current.distance.text.split(" ");
            try {
                double value = Double.parseDouble(stringArray[0]);
                total = value;
            } catch (NullPointerException | NumberFormatException e) {
                e.printStackTrace();
            }
            String unit = stringArray[1];
            finalDistance = total.toString() + " " + unit;
        }
        if(total != 0.0){
            toUpdateDistance.setText("" + finalDistance);
            toUpdateStart.setText(""+ startLocation);
            toUpdateFinish.setText(""+ endLocation);
        }
        else{
            toUpdateDistance.setText("No distance was travelled!");
            toUpdateStart.setText("Every journey has a beginning...");
            toUpdateFinish.setText("...just not this one!");
        }


    }
}
