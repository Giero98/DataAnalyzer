package com.example.masterthesis;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;
import android.bluetooth.BluetoothAdapter;

import com.example.masterthesis.bluetooh.MainActivity_BT;
import com.example.masterthesis.wifi.MainActivity_WiFi;


/**
 * Main view of the application
 */
public class MainActivity extends AppCompatActivity {

    //Log class reference
    private final MainActivity_Log.ListLog LOG = new MainActivity_Log.ListLog();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button_wifi = findViewById(R.id.button_wifi);
        Button button_bt = findViewById(R.id.button_bt);

        //Bluetooth connection button
        button_bt.setOnClickListener(v -> conditionalMethodsBt());

        //Wi-Fi connection button
        button_wifi.setOnClickListener(v -> {
            Toast.makeText(this, "Connect", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainActivity_WiFi.class);
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
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, Constants.REQUEST_BT_CONNECT);
        }
    }

    //Calling the action of granting permissions to Bluetooth_Scan
    private void permissionBtScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, Constants.REQUEST_BT_SCAN);
        }
    }

    //Calling the action of granting permissions to Bluetooth_Advertise
    private void permissionBtAdvertise() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_ADVERTISE}, Constants.REQUEST_BT_ADVERTISE);
        }
    }

    //Calling the action of granting permissions to Bluetooth_AccessFineLocation
    private void permissionBtAccessFineLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Constants.REQUEST_BT_ACCESS_FINE_LOCATION);
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
        if (Constants.bluetoothAdapter == null) {
            Toast.makeText(MainActivity.this, "Device doesn't support Bluetooth", Toast.LENGTH_SHORT).show();
            return false;
        } else return true;
    }

    //Turn on Bluetooth or go to Bluetooth Activities
    private void firstStepBt() {
        if (!Constants.bluetoothAdapter.isEnabled())
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
            case Constants.REQUEST_BT_CONNECT:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    LOG.addLog(LOG.currentDate(),"BLUETOOTH_CONNECT permission granted");
                    conditionalMethodsBt();
                }
                else LOG.addLog(LOG.currentDate(),"BLUETOOTH_CONNECT permission denied");
                break;
            case Constants.REQUEST_BT_SCAN:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    LOG.addLog(LOG.currentDate(),"BLUETOOTH_SCAN permission granted");
                    conditionalMethodsBt();
                }
                else LOG.addLog(LOG.currentDate(),"BLUETOOTH_SCAN permission denied");
                break;
            case Constants.REQUEST_BT_ADVERTISE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    LOG.addLog(LOG.currentDate(),"BLUETOOTH_ADVERTISE permission granted");
                    conditionalMethodsBt();
                }
                else LOG.addLog(LOG.currentDate(),"BLUETOOTH_ADVERTISE permission denied");
                break;
            case Constants.REQUEST_BT_ACCESS_FINE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    LOG.addLog(LOG.currentDate(),"BLUETOOTH_ACCESS_FINE_LOCATION permission granted");
                    conditionalMethodsBt();
                }
                else LOG.addLog(LOG.currentDate(),"BLUETOOTH_ACCESS_FINE_LOCATION permission denied");
                break;
        }
    }

    //Create a menu for your current activity
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem showLog = menu.findItem(R.id.show_log);
        showLog.setTitle("Show Log");
        return true;
    }

    //Create interactions for selecting items from the menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.show_log) {
            Intent intent = new Intent(this, MainActivity_Log.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}