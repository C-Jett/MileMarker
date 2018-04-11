package com.example.jett.milemarker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class startScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);

        Button startButton = (Button) this.findViewById(R.id.startNewButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                sendMessage(v);
            }
        });
    }

    //Takes intent and the second variable is the destination.
    public void sendMessage(View view) {
        Intent intent = new Intent(this, Main.class);
        startActivity(intent);
    }
}
