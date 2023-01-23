package com.example.masterthesis;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * View of the application after a successful combination via Wi-Fi
 */
public class MainActivity_WiFi extends AppCompatActivity {

    Button button_back;
    TextView text1;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_wi_fi);

        button_back = findViewById(R.id.button3);
        text1 = findViewById(R.id.textView2);

        text1.setText("Good Job!\n" +
                "You are connected by Wi-Fi.");
        button_back.setText("Disconnect");

        /*
          Button to disconnect from the connected device
         */
        button_back.setOnClickListener(v -> {

            Toast.makeText(MainActivity_WiFi.this, "Disconnect", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity_WiFi.this, MainActivity.class);
            startActivity(intent);
        });
    }
}