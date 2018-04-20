package com.example.jett.milemarker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextClock;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class startScreen extends AppCompatActivity {

    TextClock clock;

    ArrayList<ArrayList<LongLatSerial>> history = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);

        updateHistoryList();
        Button settingsButton = findViewById(R.id.settingsButton);
        Button startButton = findViewById(R.id.startNewButton);
        clock = findViewById(R.id.clockView);

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSettings(v);
            }
        });

        startButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                sendMessage(v);
            }
        });
    }

    //Makes sure the list is updated upon returning to the view.
    @Override
    protected void onResume() {
        super.onResume();
        updateHistoryList();
    }

    //Takes intent and the second variable is the destination.
    public void sendMessage(View view) {
        Intent intent = new Intent(this, Main.class);
        startActivity(intent);
    }

    public void sendSettings(View view) {
        Intent intent = new Intent(this, settings.class);
        startActivity(intent);
    }

    /**
     * intOfHistory is used to bring the data that is saved with the gMap according to the
     * trip number in start_screen.
     * @param view
     * @param intOfHistory - int of trip number
     */
    public void sendTrip(View view, Integer intOfHistory) {
        Intent intent = new Intent(this, trip_info.class);
        if (intOfHistory != null && history.size() > intOfHistory && intOfHistory >=0){
            intent.putExtra("historyIndex",intOfHistory);
        }
        startActivity(intent);
    }

    /**
     * Copied code from other locations in app.
     * Opens the serialized stream to take in data then updates the history list for showing
     * previous trips.
     */
    public void updateHistoryList() {
        ArrayList<String> toShow = new ArrayList<>();
        File directory = getFilesDir();
        File historyFile = new File(directory, "history");
        try {
            if (historyFile.exists()) {
                FileInputStream input = new FileInputStream(historyFile);
                ObjectInputStream objIn = new ObjectInputStream(input);
                Object historyObject = objIn.readObject();
                if (historyObject instanceof ArrayList) {
                    history = (ArrayList<ArrayList<LongLatSerial>>) historyObject;
                }
            }
            else {
                Log.d("FILEEXISTANCE", "File does not exist on reading for ListUpdate");
                FileOutputStream fileOut = new FileOutputStream(historyFile);
                ObjectOutputStream objOut = new ObjectOutputStream(fileOut);
                objOut.writeObject(history);
                objOut.close();
                Log.d("FILEEXISTANCEUPDATE", "File made: " + historyFile.exists());
            }
        }
        catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < history.size(); i++) {
            toShow.add("Trip " + (i + 1));
        } //Trip is added to the start_screen view and data is saved within it.


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, toShow);
        ListView historyView = findViewById(R.id.historyList);
        historyView.setAdapter(adapter);

        historyView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                sendTrip(view, position);
            }
        });
    }
}