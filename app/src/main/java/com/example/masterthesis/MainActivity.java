package com.example.masterthesis;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
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
public class MainActivity extends AppCompatActivity {
    //Constant used for check Bluetooth permission
    private static final int REQUEST_BLUETOOTH_CONNECT_PERMISSION = 1;
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
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
            if(checkSupportBt()) {
                if (checkAPI() && checkBtConnect()) {
                    permissionBtConnect();
                } else {
                    firstStepBt();
                }
            }
            //Intent intent = new Intent(MainActivity.this, MainActivity_BT.class);
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
    //Checking if the device supports Bluetooth
    private boolean checkSupportBt()
    {
        if (bluetoothAdapter == null)
        {
            Toast.makeText(MainActivity.this, "Device doesn't support Bluetooth", Toast.LENGTH_SHORT).show();
            return false;
        }
        else return true;
    }
    //Checks if the API version is >=23
    private boolean checkAPI()
    {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }
    //Checks permissions to Bluetooth_Connect
    private boolean checkBtConnect()
    {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED;
    }
    //Calling the action of granting permissions to Bluetooth_Connect
    private void permissionBtConnect()
    {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_BLUETOOTH_CONNECT_PERMISSION);
    }
    //Reactions to permission response received
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_CONNECT_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "BLUETOOTH_CONNECT permission granted", Toast.LENGTH_SHORT).show();
                firstStepBt();
            } else {
                Toast.makeText(this, "BLUETOOTH_CONNECT permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void firstStepBt() {
        if (!bluetoothAdapter.isEnabled())
        {
            enableBt();
        }
        else
        {
            detectBt();
        }
    }
    private void enableBt()
    {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        ActivityEnableBt.launch(enableBtIntent);
    }
    //Reactions to permission response received enableBT
    ActivityResultLauncher<Intent> ActivityEnableBt = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK)
                {
                    Toast.makeText(MainActivity.this, "Bluetooth started", Toast.LENGTH_SHORT).show();
                    detectBt();
                }
                else
                {Toast.makeText(MainActivity.this, "Bluetooth not enabled", Toast.LENGTH_SHORT).show();}
            });
    private void detectBt()
    {
        Intent detectBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        ActivityDetectBt.launch(detectBtIntent);
    }
    //Reactions to permission response received detectBT
    ActivityResultLauncher<Intent> ActivityDetectBt = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() != 0)
                {
                    Toast.makeText(MainActivity.this, "Detection has been enabled", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(MainActivity.this, "Detection has not been enabled", Toast.LENGTH_SHORT).show();
                }
            });
}