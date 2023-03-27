package com.bg.masterthesis.wifi;

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
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.bg.masterthesis.Buffer;
import com.bg.masterthesis.Constants;
import com.bg.masterthesis.file.SendingData;
import com.bg.masterthesis.ui.DeclarationOfUIVar;
import com.bg.masterthesis.file.FileInformation;
import com.bg.masterthesis.Graph;
import com.bg.masterthesis.Logs;
import com.bg.masterthesis.ui.NumberOfFileFromUI;
import com.bg.masterthesis.R;
import com.bg.masterthesis.file.SavingData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class WiFi extends AppCompatActivity {
    final ArrayList<String> discoveredDevices = new ArrayList<>();
    ArrayAdapter<String> listAdapter;
    static final Logs.ListLog LOG = new Logs.ListLog();
    String fileName;

    public static WifiP2pManager wifiDirectManager;
    static WifiP2pManager.Channel wifiDirectChannel;
    Intent fileToSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wi_fi);
        setTitle("Wi-Fi");

        new DeclarationOfUIVar(this);
        startSelectBufferSize();
        settingVariableManagingConnect();
        startReceiverWithFilters();

        DeclarationOfUIVar.button_disconnectBack.setOnClickListener(v -> finish());
        DeclarationOfUIVar.button_detect.setOnClickListener(v -> startDiscoversDevices());
        DeclarationOfUIVar.button_devices.setOnClickListener(v -> displayWiFiDirectDevices());
        DeclarationOfUIVar.button_chooseFile.setOnClickListener(v -> chooseFile());
        DeclarationOfUIVar.multiple_file.setOnClickListener(v -> NumberOfFileFromUI.readNumberOfFilesToSent(this));
        DeclarationOfUIVar.button_upMultipleFile.setOnClickListener(v -> NumberOfFileFromUI.increasingNumberOfFilesToSent());
        DeclarationOfUIVar.button_downMultipleFile.setOnClickListener(v -> NumberOfFileFromUI.reducingNumberOfFilesToSent());
        DeclarationOfUIVar.button_sendData.setOnClickListener(v -> sendData());
        DeclarationOfUIVar.button_saveMeasurementData.setOnClickListener(v -> saveMeasurementData());
        DeclarationOfUIVar.button_graph.setOnClickListener(v -> drawGraph());
    }

    void startSelectBufferSize()
    {
        new Buffer(this, findViewById(R.id.buffer_size));
    }

    void settingVariableManagingConnect()
    {
        wifiDirectManager = (WifiP2pManager) getSystemService(WIFI_P2P_SERVICE);
        wifiDirectChannel = wifiDirectManager.initialize(this,getMainLooper(),null);
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
            if(wifiDirectManager!=null) {
                String action = intent.getAction();
                if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action))
                {
                    wifiDirectManager.requestPeers(wifiDirectChannel, peerListListener);
                }
                else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action))
                {
                    NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                    if(networkInfo.isConnected()) {
                        wifiDirectManager.requestConnectionInfo(wifiDirectChannel, connectionInfoListener);
                    }
                    else {
                        DeclarationOfUIVar.textView_connected.setText("Not connected");
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
    WifiP2pManager.ConnectionInfoListener connectionInfoListener = wifiDirectInfo -> {
        if(wifiDirectInfo.groupFormed && wifiDirectInfo.isGroupOwner)
        {
            ServerWiFi server = new ServerWiFi(this);
            server.start();
        } else if(wifiDirectInfo.groupFormed)
        {
            discoverService(wifiDirectInfo);
        }
    };

    void discoverService(WifiP2pInfo wifiDirectInfo) {
        final HashMap<String, String> buddies = new HashMap<>();

        WifiP2pManager.DnsSdTxtRecordListener txtListener = (fullDomain, record, device) -> {

            String[] portNumberInfo = record.toString().split("=");
            String portNumber = portNumberInfo[1].replace("}","");
            LOG.addLog("portNumber:  " + portNumber);
            ClientWiFi client = new ClientWiFi(wifiDirectInfo, portNumber);
            client.start();

            buddies.put(device.deviceAddress, record.get("Port"));
        };

        WifiP2pManager.DnsSdServiceResponseListener servListener = (instanceName, registrationType, resourceType) ->
                resourceType.deviceName = buddies
                .containsKey(resourceType.deviceAddress) ? buddies
                .get(resourceType.deviceAddress) : resourceType.deviceName;

        wifiDirectManager.setDnsSdResponseListeners(wifiDirectChannel, servListener, txtListener);

        WifiP2pDnsSdServiceRequest serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        wifiDirectManager.addServiceRequest(wifiDirectChannel, serviceRequest, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                    }
                    @Override
                    public void onFailure(int code) {
                        LOG.addLog("", String.valueOf(code));
                    }
                });
        wifiDirectManager.discoverServices(wifiDirectChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {}
                @Override
                public void onFailure(int code) {
                    LOG.addLog("", String.valueOf(code));
                }
            });
    }

    void startDiscoversDevices()
    {
        wifiDirectManager.discoverPeers(wifiDirectChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(getApplicationContext(), "Discovery started", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onFailure(int i) {
                Toast.makeText(getApplicationContext(), "Discovery failed", Toast.LENGTH_SHORT).show();
                LOG.addLog("Discovery failed:", String.valueOf(i));
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
        deviceSelection.setTitle(Constants.titleDialogToSelectDevice);

        deviceSelection.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        deviceSelection.setAdapter(listAdapter, (dialog, which) -> {
            String[] deviceInfo = listAdapter.getItem(which).split("\n");
            String selectedDeviceAddress = deviceInfo[1].trim();
            initiateConnection(selectedDeviceAddress);
        });

        showDeviceSelection(deviceSelection);
    }

    void initiateConnection(String selectedDeviceAddress)
    {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = selectedDeviceAddress;
        config.groupOwnerIntent = 0; //connect as a client

        wifiDirectManager.connect(wifiDirectChannel, config, new WifiP2pManager.ActionListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onSuccess() {
                LOG.addLog("Connection initialization successful");
            }
            @Override
            public void onFailure(int reason) {
                Toast.makeText(getApplicationContext(), "Connect failed. Retry.", Toast.LENGTH_SHORT).show();
                LOG.addLog("Connection failed", String.valueOf(reason));
            }
        });
    }

    void showDeviceSelection(AlertDialog.Builder deviceSelection)
    {
        AlertDialog dialog = deviceSelection.create();
        dialog.show();
    }

    //region button_chooseFile

    void chooseFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent,Constants.REQUEST_BT_SEND_DATA_FILE);
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_BT_SEND_DATA_FILE && resultCode == RESULT_OK) {
            LOG.addLog("You have selected a file to upload");
            DeclarationOfUIVar.button_sendData.setVisibility(View.VISIBLE);
            DeclarationOfUIVar.parameterLayoutForFileUpload.setVisibility(View.VISIBLE);
            fileToSend = data;
            Double fileSize = FileInformation.getFileSize(fileToSend.getData(),this);
            fileName = FileInformation.getFileName(fileToSend.getData(), this);
            displayFileInformation(fileSize);
        }
    }

    @SuppressLint("SetTextI18n")
    void displayFileInformation(Double fileSize)
    {
        DeclarationOfUIVar.textView_inf.setText("The name of the uploaded file: " + fileName +
                "\nFile size: " + Constants.decimalFormat.format(fileSize).replace(",", ".") +
                " " + FileInformation.getFileSizeUnit(FileInformation.getFileSizeBytes()) + "\n");
    }

    //endregion

    void sendData()
    {
        new Thread(() -> {
                int multipleFile = NumberOfFileFromUI.getNumberFromUI();
                new SendingData(LOG, this,ClientWiFi.getSocket(),fileToSend,multipleFile);
        }).start();
    }

    void saveMeasurementData(){
        SendingData.saveMeasurementData(this);
    }

    void drawGraph(){
        Graph.connectionDetails = Constants.connectionWiFi;
        Intent intent = new Intent(this, Graph.class);
        startActivity(intent);
    }



    void disconnectTheConnection()
    {
        closeAllAboutWifiManager();
        endListening();
        closeClientWifiSocket();
        closeServerWifiSocket();
        closeServerWifiServerSocket();
    }

    void closeAllAboutWifiManager()
    {
        if(wifiDirectManager!=null) {
            wifiDirectManager.requestConnectionInfo(wifiDirectChannel, info -> {
                if (info.groupFormed) {
                    wifiDirectManager.removeGroup(wifiDirectChannel, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            LOG.addLog("Disconnected successfully");
                        }

                        @Override
                        public void onFailure(int reasonCode) {
                            Toast.makeText(getApplicationContext(), "Failed to disconnect", Toast.LENGTH_SHORT).show();
                            LOG.addLog("Failed to disconnect", String.valueOf(reasonCode));
                        }
                    });
                }
            });

            wifiDirectManager.stopPeerDiscovery(wifiDirectChannel, null);
            LOG.addLog("WiFi Direct has been disabled");
        }
    }

    void endListening()
    {
        if(receiver.isOrderedBroadcast()) {
            unregisterReceiver(receiver);
            LOG.addLog("Broadcast on Bt was closed");
        }
    }

    void closeClientWifiSocket()
    {
        if(ClientWiFi.getSocket() != null)
            if(ClientWiFi.getSocket().isConnected()) {
                try {
                    SendingData.closeStream();
                    ClientWiFi.getSocket().close();
                    LOG.addLog("Socket client was closed");
                } catch (IOException e) {
                    LOG.addLog("Error closing socket client", e.getMessage());
                }
            }
    }

    void closeServerWifiSocket()
    {
        if(ServerWiFi.getSocket() != null)
            if(ServerWiFi.getSocket().isConnected()) {
                try {
                    SavingData.closeStream();
                    ServerWiFi.getSocket().close();
                    LOG.addLog("Socket server was closed");
                } catch (IOException e) {
                    LOG.addLog("Error closing socket server", e.getMessage());
                }
            }
    }

    void closeServerWifiServerSocket()
    {
        if(ServerWiFi.getServerSocket() != null) {
            try {
                ServerWiFi.getServerSocket().close();
                LOG.addLog("ServerSocket server was closed");
            } catch (IOException e) {
                LOG.addLog("Error closing ServerSocket server", e.getMessage());
            }
        }
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