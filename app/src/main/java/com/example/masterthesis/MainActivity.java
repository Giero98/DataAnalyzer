package com.example.masterthesis;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;
import android.bluetooth.BluetoothAdapter;

import com.example.masterthesis.bluetooh.Bluetooth;
import com.example.masterthesis.wifi.WiFi;

public class MainActivity extends AppCompatActivity {
    final Logs.ListLog LOG = new Logs.ListLog();
    WifiManager wifiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        new Permissions(this);

        if (!Permissions.checkAccessFineLocation())
            Permissions.getPermissionAccessFineLocation();

        Button button_wifi = findViewById(R.id.button_wifi);
        Button button_bt = findViewById(R.id.button_bt);

        button_bt.setOnClickListener(v -> checkingPermissionsBt());
        button_wifi.setOnClickListener(v -> checkingPermissionsWiFi());
    }

    //region configureBT

    void goToBtActivity()
    {
        Intent intent = new Intent(this, Bluetooth.class);
        startActivity(intent);
    }

    public void checkingPermissionsBt()
    {
        if (checkSupportBt())
        {
            if (!Permissions.checkBtConnect())
            {
                Permissions.getPermissionBtConnect();
            }
            else if (!Permissions.checkBtScan())
            {
                Permissions.getPermissionBtScan();
            }
            else if (!Permissions.checkBtAdvertise())
            {
                Permissions.getPermissionBtAdvertise();
            }
            else
            {
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

    //endregion

    //region configureWiFi

    void goToWiFiActivity() {
        Intent intent = new Intent(this, WiFi.class);
        startActivity(intent);
    }

    public void checkingPermissionsWiFi()
    {

        if(!Permissions.checkAccessWiFiState())
        {
            Permissions.getPermissionAccessWiFiState();
        }
        else if(!Permissions.checkChangeWiFiState())
        {
            Permissions.getPermissionChangeWiFiState();
        }
        else if(!Permissions.checkInternet())
        {
            Permissions.getPermissionInternet();
        }
        else if(!Permissions.checkAccessNetworkState())
        {
            Permissions.getPermissionAccessNetworkState();
        }
        else if(!Permissions.checkChangeNetworkState())
        {
            Permissions.getPermissionChangeNetworkState();
        }
        else if(!Permissions.checkNearbyWiFiDevices())
        {
            Permissions.getPermissionNearbyWiFiDevices();
        }
        else if (!wifiManager.isWifiEnabled())
        {
            enableWiFi();
        }
        else if(checkSupportWiFiDirect())
        {
            goToWiFiActivity();
        }
    }

    boolean checkSupportWiFiDirect() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI_DIRECT)) {
            Toast.makeText(this, "Device doesn't support WiFi Direct", Toast.LENGTH_SHORT).show();
            return false;
        } else return true;
    }

    void enableWiFi() {
        if(Permissions.checkAPI29()) {
            Intent intent = new Intent(Settings.Panel.ACTION_WIFI);
            ActivityEnableWiFi.launch(intent);
        } else {
            wifiManager.setWifiEnabled(true);
            Toast.makeText(this, "WiFi started", Toast.LENGTH_SHORT).show();
            checkingPermissionsWiFi();
        }
    }

    final ActivityResultLauncher<Intent> ActivityEnableWiFi = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (wifiManager.isWifiEnabled()) {
                    Toast.makeText(this, "WiFi started", Toast.LENGTH_SHORT).show();
                    checkingPermissionsWiFi();
                }
                else
                    Toast.makeText(this, "WiFi not enabled", Toast.LENGTH_SHORT).show();
            });

    //endregion

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
            case Constants.REQUEST_ACCESS_FINE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    LOG.addLog("ACCESS_FINE_LOCATION permission granted");
                else
                    LOG.addLog("ACCESS_FINE_LOCATION permission denied");
                break;
            case Constants.REQUEST_CHANGE_WIFI_STATE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    LOG.addLog("CHANGE_WIFI_STATE permission granted");
                    checkingPermissionsWiFi();
                }
                else LOG.addLog("CHANGE_WIFI_STATE permission denied");
                break;
            case Constants.REQUEST_ACCESS_WIFI_STATE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    LOG.addLog("ACCESS_WIFI_STATE permission granted");
                    checkingPermissionsWiFi();
                }
                else LOG.addLog("ACCESS_WIFI_STATE permission denied");
                break;
            case Constants.REQUEST_INTERNET:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    LOG.addLog("INTERNET permission granted");
                    checkingPermissionsWiFi();
                }
                else LOG.addLog("INTERNET permission denied");
                break;
            case Constants.REQUEST_NEARBY_WIFI_DEVICES:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    LOG.addLog("NEARBY_WIFI_DEVICES permission granted");
                    checkingPermissionsWiFi();
                }
                else LOG.addLog("NEARBY_WIFI_DEVICES permission denied");
                break;
            case Constants.REQUEST_ACCESS_NETWORK_STATE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    LOG.addLog("ACCESS_NETWORK_STATE permission granted");
                    checkingPermissionsWiFi();
                }
                else LOG.addLog("ACCESS_NETWORK_STATE permission denied");
                break;
            case Constants.REQUEST_CHANGE_NETWORK_STATE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    LOG.addLog("CHANGE_NETWORK_STATE permission granted");
                    checkingPermissionsWiFi();
                }
                else LOG.addLog("CHANGE_NETWORK_STATE permission denied");
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