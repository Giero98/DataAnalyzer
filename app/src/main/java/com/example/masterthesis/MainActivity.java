package com.example.masterthesis;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Main view of the application
 */
public class MainActivity extends AppCompatActivity {

    Button button_wifi,button_bt;
    TextView text1;


    @SuppressLint("SetTextI18n")/*
          Used to eliminate the problem
          "String Literal in` Settext` can not be translated. Use Android Resources Inspes. "
          to set the text on Buttons and TextView
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button_wifi = findViewById(R.id.button);
        button_bt = findViewById(R.id.button2);
        text1 = findViewById(R.id.textView);

        text1.setText("Welcome to my App");
        button_bt.setText("Connect by Bluetooth");
        button_wifi.setText("Connect by Wi-Fi");

        /*
          Bluetooth connection button
         */
        button_bt.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Connect", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, MainActivity_BT.class);
            startActivity(intent);
        });

        /*
          Wi-Fi connection button
         */
        button_wifi.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Connect", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, MainActivity_WiFi.class);
            startActivity(intent);
        });
    }
}