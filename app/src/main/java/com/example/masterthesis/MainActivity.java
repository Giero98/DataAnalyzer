package com.example.masterthesis;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.bluetooth.BluetoothAdapter;

/**
 * Main view of the application
 */
@SuppressWarnings("deprecation")
public class MainActivity extends AppCompatActivity {

    //Constant used for identification
    private static final int REQUEST_BLUETOOTH_PERMISSION = 1;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_BLUETOOTH_CONNECT_PERMISSION = 1;

    Button button_wifi, button_bt;
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

            configurationBT();
            Intent intent = new Intent(MainActivity.this, MainActivity_BT.class);
            //startActivity(intent);
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
    private void configurationBT()
    {
        //Checks permissions to Bluetooth
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED)
        {
            Toast.makeText(MainActivity.this, "Didn't get BLUETOOTH permission", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH}, REQUEST_BLUETOOTH_PERMISSION);
            Toast.makeText(MainActivity.this, "Getting BLUETOOTH permission succeded", Toast.LENGTH_SHORT).show();
        }
        else
        {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            // Code that will only be executed on devices with API >= 23 , M = Mashmallow
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                //Checks permissions to Bluetooth_Connect
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)
                {
                    Toast.makeText(MainActivity.this, "Didn't get BLUETOOTH_CONNECT permission yet", Toast.LENGTH_SHORT).show();
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_BLUETOOTH_CONNECT_PERMISSION);
                }
                else
                {
                    BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                    if (bluetoothManager != null)
                    {
                        if (bluetoothAdapter == null)
                        {
                            Toast.makeText(MainActivity.this, "Device doesn't support Bluetooth", Toast.LENGTH_SHORT).show();
                        } else if (!bluetoothAdapter.isEnabled())
                        {
                            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivityForResult(enableBtIntent,REQUEST_ENABLE_BT);
                            Toast.makeText(MainActivity.this, "Bluetooth has been activated", Toast.LENGTH_SHORT).show();
                        } else
                        {
                            Toast.makeText(MainActivity.this, "Bluetooth is active", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
            // Code that will only be executed on devices with API < 23
            else
            {
                if (bluetoothAdapter == null)
                {
                    Toast.makeText(MainActivity.this, "Device doesn't support Bluetooth", Toast.LENGTH_SHORT).show();
                } else if (!bluetoothAdapter.isEnabled())
                {
                    bluetoothAdapter.enable();
                    Toast.makeText(MainActivity.this, "Bluetooth has been activated", Toast.LENGTH_SHORT).show();
                } else
                {
                    Toast.makeText(MainActivity.this, "Bluetooth is active", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}