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
    //Variables used to match the permission response
    final private int
            REQUEST_BT_CONNECT = 0,
            REQUEST_BT_SCAN = 1,
            REQUEST_BT_ADVERTISE = 2,
            REQUEST_BT_ACCESS_FINE_LOCATION = 3;
    final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
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

        //Bluetooth connection button
        button_bt.setOnClickListener(v -> conditionalMethodsBt());

        //Wi-Fi connection button
        button_wifi.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Connect", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, MainActivity_WiFi.class);
            startActivity(intent);
        });
    }

    //Checks if the API version is >=23
    private boolean checkAPI() {

        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S;
    }

    //region checkBT

    //Checks permissions to Bluetooth_Connect
    private boolean checkBtConnect() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
        }
        else return true;
    }

    //Checks permissions to Bluetooth_Scan
    private boolean checkBtScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED;
        }
        else return false;
    }

    //Checks permissions to Bluetooth_Advertise
    private boolean checkBtAdvertise() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED;
        }
        else return false;
    }

    //Checks permissions to Bluetooth_AccessFineLocation
    private boolean checkBtAccessFineLocation() {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    //endregion checkBT

    //region permissionBT

    //Calling the action of granting permissions to Bluetooth_Connect
    private void permissionBtConnect() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_BT_CONNECT);
        }
    }

    //Calling the action of granting permissions to Bluetooth_Scan
    private void permissionBtScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, REQUEST_BT_SCAN);
        }
    }

    //Calling the action of granting permissions to Bluetooth_Advertise
    private void permissionBtAdvertise() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_ADVERTISE}, REQUEST_BT_ADVERTISE);
        }
    }

    //Calling the action of granting permissions to Bluetooth_AccessFineLocation
    private void permissionBtAccessFineLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_BT_ACCESS_FINE_LOCATION);
        }
    }

    //endregion permissionBT

    //region configureBT

    //Launching the Bluetooth Activities
    private void goToBt()
    {
        Intent intent = new Intent(MainActivity.this, MainActivity_BT.class);
        startActivity(intent);
    }

    //A method composed of conditional functions that check Bluetooth support and permissions
    private void conditionalMethodsBt()
    {
        if (checkSupportBt()) {
            if (checkAPI()) {
                if (!checkBtConnect()) {
                    permissionBtConnect();
                } else if (!checkBtScan()) {
                    permissionBtScan();
                } else if (!checkBtAdvertise()) {
                    permissionBtAdvertise();
                } else if (!checkBtAccessFineLocation()) {
                    permissionBtAccessFineLocation();
                } else {
                    firstStepBt();
                }
            } else {
                firstStepBt();
            }
        }
    }

    //Checking if the device supports Bluetooth
    private boolean checkSupportBt() {
        if (bluetoothAdapter == null) {
            Toast.makeText(MainActivity.this, "Device doesn't support Bluetooth", Toast.LENGTH_SHORT).show();
            return false;
        } else return true;
    }

    //Turn on Bluetooth or go to Bluetooth Activities
    private void firstStepBt() {
        if (!bluetoothAdapter.isEnabled())
            enableBt();
        else
            goToBt();
    }

    //Launching the Bluetooth enablement intent on the device
    private void enableBt() {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        ActivityEnableBt.launch(intent);
    }
    //Reactions to permission response received enableBt
    final ActivityResultLauncher<Intent> ActivityEnableBt = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Toast.makeText(MainActivity.this, "Bluetooth started", Toast.LENGTH_SHORT).show();
                    goToBt();
                }
                else
                    Toast.makeText(MainActivity.this, "Bluetooth not enabled", Toast.LENGTH_SHORT).show();
            });

    //endregion configureBT

    //Reactions to permission response received
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_BT_CONNECT:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "BLUETOOTH_CONNECT permission granted", Toast.LENGTH_SHORT).show();
                    conditionalMethodsBt();
                }
                else Toast.makeText(this, "BLUETOOTH_CONNECT permission denied", Toast.LENGTH_SHORT).show();
                break;
            case REQUEST_BT_SCAN:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "BLUETOOTH_SCAN permission granted", Toast.LENGTH_SHORT).show();
                    conditionalMethodsBt();
                }
                else Toast.makeText(this, "BLUETOOTH_SCAN permission denied", Toast.LENGTH_SHORT).show();
                break;
            case REQUEST_BT_ADVERTISE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "BLUETOOTH_ADVERTISE permission granted", Toast.LENGTH_SHORT).show();
                    conditionalMethodsBt();
                }
                else Toast.makeText(this, "BLUETOOTH_ADVERTISE permission denied", Toast.LENGTH_SHORT).show();
                break;
            case REQUEST_BT_ACCESS_FINE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "BLUETOOTH_ACCESS_FINE_LOCATION permission granted", Toast.LENGTH_SHORT).show();
                    conditionalMethodsBt();
                }
                else Toast.makeText(this, "BLUETOOTH_ACCESS_FINE_LOCATION permission denied", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}