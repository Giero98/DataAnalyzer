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

import com.example.masterthesis.bluetooh.Bluetooth;
import com.example.masterthesis.wifi.MainActivity_WiFi;

public class MainActivity extends AppCompatActivity {
    final Logs.ListLog LOG = new Logs.ListLog();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button_wifi = findViewById(R.id.button_wifi);
        Button button_bt = findViewById(R.id.button_bt);

        button_bt.setOnClickListener(v -> checkingPermissionsBt());

        button_wifi.setOnClickListener(v -> {
            Toast.makeText(this, "Connect", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainActivity_WiFi.class);
            startActivity(intent);
        });
    }


    boolean checkAPI() {
        // >= API 31 or >= Android 11
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S;
    }

    //region sectionBT

    //region checkBT

    boolean checkBtConnect() {
        if (checkAPI()) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
        }
        else return true;
    }
    boolean checkBtScan() {
        if (checkAPI()) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED;
        }
        else return false;
    }
    boolean checkBtAdvertise() {
        if (checkAPI()) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED;
        }
        else return false;
    }
    boolean checkBtAccessFineLocation() {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    //endregion checkBT

    //region permissionBT

    void permissionBtConnect() {
        if (checkAPI()) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, Constants.REQUEST_BT_CONNECT);
        }
    }
    void permissionBtScan() {
        if (checkAPI()) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, Constants.REQUEST_BT_SCAN);
        }
    }
    void permissionBtAdvertise() {
        if (checkAPI()) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_ADVERTISE}, Constants.REQUEST_BT_ADVERTISE);
        }
    }
    void permissionBtAccessFineLocation() {
        if (checkAPI()) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Constants.REQUEST_BT_ACCESS_FINE_LOCATION);
        }
    }

    //endregion permissionBT

    //region configureBT

    void goToBtActivity()
    {
        Intent intent = new Intent(this, Bluetooth.class);
        startActivity(intent);
    }

    void checkingPermissionsBt()
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
                    thatBtIsOn();
                }
            } else {
                thatBtIsOn();
            }
        }
    }

    boolean checkSupportBt() {
        if (Constants.bluetoothAdapter == null) {
            Toast.makeText(this, "Device doesn't support Bluetooth", Toast.LENGTH_SHORT).show();
            return false;
        } else return true;
    }

    void thatBtIsOn() {
        if (!Constants.bluetoothAdapter.isEnabled())
            enableBt();
        else
            goToBtActivity();
    }

    void enableBt() {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        ActivityEnableBt.launch(intent);
    }
    final ActivityResultLauncher<Intent> ActivityEnableBt = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Toast.makeText(this, "Bluetooth started", Toast.LENGTH_SHORT).show();
                    goToBtActivity();
                }
                else
                    Toast.makeText(this, "Bluetooth not enabled", Toast.LENGTH_SHORT).show();
            });

    //endregion configureBT

    //endregion sectionBT

    //Reactions to permission response received
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constants.REQUEST_BT_CONNECT:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    LOG.addLog("BLUETOOTH_CONNECT permission granted");
                    checkingPermissionsBt();
                }
                else LOG.addLog("BLUETOOTH_CONNECT permission denied");
                break;
            case Constants.REQUEST_BT_SCAN:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    LOG.addLog("BLUETOOTH_SCAN permission granted");
                    checkingPermissionsBt();
                }
                else LOG.addLog("BLUETOOTH_SCAN permission denied");
                break;
            case Constants.REQUEST_BT_ADVERTISE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    LOG.addLog("BLUETOOTH_ADVERTISE permission granted");
                    checkingPermissionsBt();
                }
                else LOG.addLog("BLUETOOTH_ADVERTISE permission denied");
                break;
            case Constants.REQUEST_BT_ACCESS_FINE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    LOG.addLog("BLUETOOTH_ACCESS_FINE_LOCATION permission granted");
                    checkingPermissionsBt();
                }
                else LOG.addLog("BLUETOOTH_ACCESS_FINE_LOCATION permission denied");
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem showLog = menu.findItem(R.id.show_log);
        showLog.setTitle(Constants.titleLog);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.show_log) {
            Intent intent = new Intent(this, Logs.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}