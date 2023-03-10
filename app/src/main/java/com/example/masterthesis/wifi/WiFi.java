package com.example.masterthesis.wifi;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.masterthesis.Constants;
import com.example.masterthesis.Logs;
import com.example.masterthesis.R;

import java.util.ArrayList;

public class WiFi extends AppCompatActivity {
    final ArrayList<String> discoveredDevices = new ArrayList<>();
    ArrayAdapter<String> listAdapter;
    final Logs.ListLog LOG = new Logs.ListLog();

    String deviceName, deviceAddress;
    WifiP2pManager wifiP2pManager;
    WifiP2pManager.Channel wifiP2pChannel;
    TextView textView_connected, textView_inf, textView_qualitySignal;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wi_fi);
        setTitle("Wi-Fi");

        Button  button_back = findViewById(R.id.button_disconnectAndBack),
                button_detect = findViewById(R.id.button_detect),
                button_deviceDisplay = findViewById(R.id.button_deviceDisplay);
        textView_connected = findViewById(R.id.textView_connected);
        textView_inf = findViewById(R.id.textView_inf);
        textView_qualitySignal = findViewById(R.id.textView_qualitySignal);

        textView_connected.setText("Not connected");

        settingVarManagingConnect();
        startReceiverWithFilters();

        button_back.setOnClickListener(v -> finish());
        button_detect.setOnClickListener(v -> startDiscoversDevices());
        button_deviceDisplay.setOnClickListener(v -> displayWiFiDirectDevices());

    }

    void settingVarManagingConnect()
    {
        wifiP2pManager = (WifiP2pManager) getSystemService(WIFI_P2P_SERVICE);
        wifiP2pChannel = wifiP2pManager.initialize(this,getMainLooper(),null);
    }

    void startReceiverWithFilters()
    {
        createListDiscoverableDevices();
        IntentFilter intent = new IntentFilter();
        intent.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intent.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        registerReceiver(receiver, intent);
    }

    void createListDiscoverableDevices()
    {
        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, discoveredDevices);
        listAdapter.clear();
    }

    final BroadcastReceiver receiver = new BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if(wifiP2pManager!=null) {
                if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action))
                {
                    wifiP2pManager.requestPeers(wifiP2pChannel, peerListListener);
                }
                else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action))
                {
                    NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                    if(networkInfo.isConnected()) {
                        wifiP2pManager.requestConnectionInfo(wifiP2pChannel, connectionInfoListener);
                    }
                    else {
                        textView_connected.setText("Not Connected");
                    }
                }
            }
        }
    };

    WifiP2pManager.PeerListListener peerListListener = peerList -> {
        if(peerList.getDeviceList() != null)
        {
            discoveredDevices.clear();
            for(WifiP2pDevice device : peerList.getDeviceList())
            {
                discoveredDevices.add(device.deviceName + "\n" + device.deviceAddress);
                listAdapter.notifyDataSetChanged();
            }
        } else {
            Toast.makeText(getApplicationContext(), "No Devices Found", Toast.LENGTH_SHORT).show();
        }
    };

    @SuppressLint("SetTextI18n")
    WifiP2pManager.ConnectionInfoListener connectionInfoListener = wifiP2pInfo -> {
        if(wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner)
        {
            textView_connected.setText("Connected as a server");
            ServerWiFi server = new ServerWiFi(LOG);
            server.start();
        } else if(wifiP2pInfo.groupFormed)
        {
            textView_connected.setText("Connected as a client");
            ClientWiFi client = new ClientWiFi(LOG, deviceAddress);
            client.start();
        } else {
            textView_connected.setText("Not Connected");
        }
    };

    void startDiscoversDevices()
    {
        wifiP2pManager.discoverPeers(wifiP2pChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(getApplicationContext(), "Searching for devices using WiFi Direct was successful", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onFailure(int i) {
                Toast.makeText(getApplicationContext(), "Searching for devices using WiFi Direct failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    void displayWiFiDirectDevices()
    {
        startDiscoversDevices();
        selectDeviceToConnection();
    }

    @SuppressLint("MissingPermission")
    void selectDeviceToConnection()
    {
        AlertDialog.Builder deviceSelection = new AlertDialog.Builder(this);
        @SuppressLint("InflateParams")
        View titleView = getLayoutInflater().inflate(R.layout.window_device_selection, null);
        deviceSelection.setCustomTitle(titleView);
        deviceSelection.setTitle("Select a device");

        deviceSelection.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        deviceSelection.setAdapter(listAdapter, (dialog, which) -> {
            String[] deviceInfo = listAdapter.getItem(which).split("\n");
            deviceName = deviceInfo[0].trim();
            deviceAddress = deviceInfo[1].trim();
            checkConnectionStatus();
        });

        showDeviceSelection(deviceSelection);
    }

    void checkConnectionStatus()
    {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = deviceAddress;
        wifiP2pManager.connect(wifiP2pChannel, config, new WifiP2pManager.ActionListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onSuccess() {
                LOG.addLog("Successfully to connect to " + deviceName);
                startVisibilityQualitySignal();
            }
            @Override
            public void onFailure(int reason) {
                Toast.makeText(getApplicationContext(), "Failed to connect to " + deviceName, Toast.LENGTH_SHORT).show();
            }
        });
    }

    void startVisibilityQualitySignal() {
        TextView qualitySignalText = findViewById(R.id.textView_qualitySignalText);
        qualitySignalText.setVisibility(View.VISIBLE);
    }

    void showDeviceSelection(AlertDialog.Builder deviceSelection)
    {
        AlertDialog dialog = deviceSelection.create();
        dialog.show();
    }

    void disconnectTheConnection()
    {
        wifiP2pManager.removeGroup(wifiP2pChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                LOG.addLog("Disconnected successfully with " + deviceName);
            }
            @Override
            public void onFailure(int reasonCode) {
                LOG.addLog("Failed to disconnect with " + deviceName);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnectTheConnection();
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