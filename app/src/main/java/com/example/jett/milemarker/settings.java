package com.example.jett.milemarker;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.google.android.gms.maps.GoogleMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

public class settings extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        Button deleteTrip = findViewById(R.id.deleteTripButton);
        Switch nightMode = findViewById(R.id.nightMode);


        nightMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){

                }
                else{

                }
            }
        });

        deleteTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deletePolyLineFromHistory();
                Intent intent = new Intent(getApplicationContext(), startScreen.class);
                startActivity(intent);
            }
        });
    }


    void deletePolyLineFromHistory() {
        File directory = getFilesDir();
        File historyFile = new File(directory, "history");
        historyFile.delete();
    }
}
