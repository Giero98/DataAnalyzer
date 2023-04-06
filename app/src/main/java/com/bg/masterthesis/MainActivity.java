package com.bg.masterthesis;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;
import android.bluetooth.BluetoothAdapter;

import com.bg.masterthesis.bluetooh.Bluetooth;
import com.bg.masterthesis.wifi.WiFi;

public class MainActivity extends AppCompatActivity {
    final Logs LOG = new Logs();
    Permissions permissions;
    WifiManager wifiManager;
    Button button_wifi, button_bt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializePermissionAndGetWifiManager();
        declarationFirstButtons();
        buttonsResponses();
    }

    void initializePermissionAndGetWifiManager() {
        permissions = new Permissions(this);
        checkAndRequestPermissionForAccessFineLocation();
        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
    }

    void checkAndRequestPermissionForAccessFineLocation() {
        if (!permissions.checkAccessFineLocation())
            permissions.getPermissionAccessFineLocation();
    }

    void declarationFirstButtons() {
        button_wifi = findViewById(R.id.button_wifi);
        button_bt = findViewById(R.id.button_bt);
    }

    void buttonsResponses() {
        button_bt.setOnClickListener(v -> checkAndDoAction(this::checkingBtSetting));
        button_wifi.setOnClickListener(v -> checkAndDoAction(this::checkingWifiSetting));
    }

    void checkAndDoAction(Runnable action) {
        if (checkLocationIsOn()) {
            action.run();
        } else {
            requestToTurnOnLocation();
        }
    }

    boolean checkLocationIsOn() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    void requestToTurnOnLocation() {
        Toast.makeText(this,"Please Turn On Location", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
    }

    //region configureBT

    void checkingBtSetting() {
        if (checkSupportBt()) {
            if(permissions.checkPermissionBt()) {
                checkBtIsOn();
            }
        }
    }

    boolean checkSupportBt() {
        if (Constants.bluetoothAdapter == null) {
            Toast.makeText(this, "Device doesn't support Bluetooth", Toast.LENGTH_SHORT).show();
            return false;
        }
        else return true;
    }

    void checkBtIsOn() {
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

    void goToBtActivity() {
        Intent intent = new Intent(this, Bluetooth.class);
        startActivity(intent);
    }

    //endregion

    //region configureWiFi

    void checkingWifiSetting() {
        if(permissions.checkPermissionWifi()) {
            checkWifiIsOnIfNotTurnOn();
        }
    }

    void checkWifiIsOnIfNotTurnOn() {
        if (!wifiManager.isWifiEnabled()) {
            enableWiFi();
        }
        else checkWifiDirectAvailableIfYesStartWifiActivity();
    }

    void enableWiFi() {
        if(permissions.checkAPI29()) {
            Intent intent = new Intent(Settings.Panel.ACTION_WIFI);
            ActivityEnableWiFi.launch(intent);
        }
        else {
            wifiManager.setWifiEnabled(true);
            Toast.makeText(this, "WiFi started", Toast.LENGTH_SHORT).show();
            checkWifiDirectAvailableIfYesStartWifiActivity();
        }
    }

    final ActivityResultLauncher<Intent> ActivityEnableWiFi = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (wifiManager.isWifiEnabled()) {
                    Toast.makeText(this, "WiFi started", Toast.LENGTH_SHORT).show();
                    checkWifiDirectAvailableIfYesStartWifiActivity();
                }
                else
                    Toast.makeText(this, "WiFi not enabled", Toast.LENGTH_SHORT).show();
            });

    void checkWifiDirectAvailableIfYesStartWifiActivity() {
        if (checkSupportWiFiDirect()) {
            goToWiFiActivity();
        }
    }

    boolean checkSupportWiFiDirect() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI_DIRECT)) {
            Toast.makeText(this, "Device doesn't support WiFi Direct", Toast.LENGTH_SHORT).show();
            return false;
        } else return true;
    }

    void goToWiFiActivity() {
        Intent intent = new Intent(this, WiFi.class);
        startActivity(intent);
    }

    //endregion

    //Reactions to permission response received
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(Constants.REQUEST_ACCESS_FINE_LOCATION==requestCode) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                LOG.addLog("ACCESS_FINE_LOCATION permission granted");
            else
                LOG.addLog("ACCESS_FINE_LOCATION permission denied");
        }
        else if(Constants.requestBtCodes.contains(requestCode)) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkingBtSetting();
            }
        }
        else if(Constants.requestWifiCodes.contains(requestCode)) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkingWifiSetting();
            }
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