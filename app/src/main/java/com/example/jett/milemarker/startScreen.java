package com.example.jett.milemarker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class startScreen extends AppCompatActivity {

    ArrayList<ArrayList<LongLatSerial>> history = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);

        updateHistoryList();

        Button startButton = findViewById(R.id.startNewButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                sendMessage(v);
            }
        });
    }

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
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, toShow);
        ListView historyView = findViewById(R.id.historyList);
        historyView.setAdapter(adapter);
    }
}