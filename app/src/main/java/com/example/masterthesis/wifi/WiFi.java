package com.example.masterthesis.wifi;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.masterthesis.Buffer;
import com.example.masterthesis.Constants;
import com.example.masterthesis.Logs;
import com.example.masterthesis.R;
import com.example.masterthesis.SendReceive;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class WiFi extends AppCompatActivity {
    final ArrayList<String> discoveredDevices = new ArrayList<>();
    ArrayAdapter<String> listAdapter;
    static final Logs.ListLog LOG = new Logs.ListLog();
    long fileSizeBytes;
    String selectedDeviceName, fileSizeUnit = Constants.fileSizeUnitBytes, fileName;
    static WifiP2pManager wifiDirectManager;
    static WifiP2pManager.Channel wifiDirectChannel;
    TextView textView_connected, textView_inf, textView_qualitySignal;
    EditText multiple_file;
    Button button_detect, button_disconnectBack, button_chooseFile, button_deviceDisplay,
            button_sendData, button_saveMeasurementData, button_graph;
    LinearLayout parameterLayoutForFileUpload, layoutPercent;
    Intent fileToSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wi_fi);
        setTitle("Wi-Fi");

        button_disconnectBack = findViewById(R.id.button_disconnectAndBack);
        button_detect = findViewById(R.id.button_detect);
        button_deviceDisplay = findViewById(R.id.button_deviceDisplay);
        button_chooseFile = findViewById(R.id.button_chooseFile);
        multiple_file = findViewById(R.id.multiple_file);
        ImageButton button_upMultipleFile = findViewById(R.id.button_upMultipleFile),
                    button_downMultipleFile = findViewById(R.id.button_downMultipleFile);
        button_sendData = findViewById(R.id.button_sendData);
        button_saveMeasurementData = findViewById(R.id.button_saveMeasurementData);
        button_graph = findViewById(R.id.button_graph);

        textView_connected = findViewById(R.id.textView_connected);
        textView_inf = findViewById(R.id.textView_inf);
        textView_qualitySignal = findViewById(R.id.textView_qualitySignal);

        parameterLayoutForFileUpload = findViewById(R.id.parameterLayoutForFileUpload);
        layoutPercent = findViewById(R.id.layoutPercent);

        startSelectBufferSize();
        settingVariableManagingConnect();
        startReceiverWithFilters();

        button_disconnectBack.setOnClickListener(v -> finish());
        button_detect.setOnClickListener(v -> startDiscoversDevices());
        button_deviceDisplay.setOnClickListener(v -> displayWiFiDirectDevices());
        button_chooseFile.setOnClickListener(v -> chooseFile());
        multiple_file.setOnClickListener(v -> readNumberOfFilesToSent());
        button_upMultipleFile.setOnClickListener(v -> increasingNumberOfFilesToSent());
        button_downMultipleFile.setOnClickListener(v -> reducingNumberOfFilesToSent());
        button_sendData.setOnClickListener(v -> sendData());
        button_saveMeasurementData.setOnClickListener(v -> {});
        button_graph.setOnClickListener(v -> {});
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
                        textView_connected.setText("Not connected");
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
            ServerWiFi server = new ServerWiFi(LOG,this, selectedDeviceName);
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
            ClientWiFi client = new ClientWiFi(LOG,this, wifiDirectInfo, portNumber);
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
                        LOG.addLog("test 0002");
                    }
                    @Override
                    public void onFailure(int code) {
                        LOG.addLog("", String.valueOf(code));
                    }
                });
        wifiDirectManager.discoverServices(wifiDirectChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    LOG.addLog("test 0001");
                }
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
            selectedDeviceName = deviceInfo[0].trim();
            String selectedDeviceAddress = deviceInfo[1].trim();
            initiateConnection(selectedDeviceAddress);
        });

        showDeviceSelection(deviceSelection);
    }

    void initiateConnection(String selectedDeviceAddress)
    {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = selectedDeviceAddress;
        config.groupOwnerIntent = 15; //connect as a server

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

    void chooseFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent,Constants.REQUEST_BT_SEND_DATA_FILE);
    }

    @SuppressLint({"MissingPermission", "SetTextI18n"})
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_BT_SEND_DATA_FILE && resultCode == RESULT_OK) {
            LOG.addLog("You have selected a file to upload");
            button_sendData.setVisibility(View.VISIBLE);
            parameterLayoutForFileUpload.setVisibility(View.VISIBLE);
            fileToSend = data;
            Double fileSize = getFileSize(fileToSend.getData());
            fileName = getFileName(fileToSend.getData());
            textView_inf.setText("The name of the uploaded file: " + fileName + "\nFile size: " +
                    Constants.decimalFormat.format(fileSize).replace(",", ".") +
                    " " + fileSizeUnit + "\n");
        }
    }

    @SuppressLint("Range")
    public double getFileSize(Uri uri)
    {
        File file = new File(uri.getPath());
        double fileSize = 0;
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                fileSize = cursor.getDouble(cursor.getColumnIndex(OpenableColumns.SIZE));
            }
        }
        if(fileSize == 0)
            fileSize = file.length();
        fileSizeBytes = (long) fileSize;
        if(fileSize > Constants.size1Kb) {
            fileSize /= Constants.size1Kb;
            fileSizeUnit = Constants.fileSizeUnitKB;
            if(fileSize > Constants.size1Kb) {
                fileSize /= Constants.size1Kb;
                fileSizeUnit = Constants.fileSizeUnitMB;
            }
        }
        return fileSize;
    }

    @SuppressLint("Range")
    public String getFileName(Uri uri)
    {
        String fileName = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if(fileName != null) {
            int cut = fileName.lastIndexOf('/');
            if (cut != -1) {
                fileName = fileName.substring(cut + 1);
            }
        }
        return fileName;
    }

    @SuppressLint("SetTextI18n")
    void readNumberOfFilesToSent()
    {
        try {
            int number = Integer.parseInt(multiple_file.getText().toString());
            if(number < Constants.minimumNumberOfUploadFiles || number > Constants.maximumNumberOfUploadFiles)
            {
                Toast.makeText(this, "Enter a value between 1-100", Toast.LENGTH_SHORT).show();
                if(number > Constants.maximumNumberOfUploadFiles)
                    multiple_file.setText(Integer.toString(Constants.maximumNumberOfUploadFiles));
            }
        } catch(NumberFormatException e) {
            Toast.makeText(this, "Enter a numeric value", Toast.LENGTH_SHORT).show();
            LOG.addLog("Incorrect format loaded", e.getMessage());
        }
    }

    @SuppressLint("SetTextI18n")
    void increasingNumberOfFilesToSent()
    {
        int number = Integer.parseInt(multiple_file.getText().toString());
        if(number < Constants.maximumNumberOfUploadFiles) {
            number += 1;
            multiple_file.setText(Integer.toString(number));
        }
    }

    @SuppressLint("SetTextI18n")
    void reducingNumberOfFilesToSent()
    {
        int number = Integer.parseInt(multiple_file.getText().toString());
        if(number > Constants.minimumNumberOfUploadFiles) {
            number -= 1;
            multiple_file.setText(Integer.toString(number));
        }
    }

    void disconnectTheConnection()
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
        if(receiver.isOrderedBroadcast()) {
            unregisterReceiver(receiver);
            LOG.addLog("Broadcast was closed");
        }
        if(ClientWiFi.getSocket() != null)
            if(ClientWiFi.getSocket().isConnected()) {
                try {
                    ClientWiFi.getSocket().close();
                    LOG.addLog("Socket client was closed");
                } catch (IOException e) {
                    LOG.addLog("Error closing socket client", e.getMessage());
                }
            }
        if(ServerWiFi.getSocket() != null)
            if(ServerWiFi.getSocket().isConnected()) {
                try {
                    ServerWiFi.getSocket().close();
                    LOG.addLog("Socket server was closed");
                } catch (IOException e) {
                    LOG.addLog("Error closing socket server", e.getMessage());
                }
            }
        if(ServerWiFi.getServerSocket() != null) {
                try {
                    ServerWiFi.getServerSocket().close();
                    LOG.addLog("ServerSocket server was closed");
                } catch (IOException e) {
                    LOG.addLog("Error closing ServerSocket server", e.getMessage());
                }
            }
    }

    void sendData()
    {
        new Thread(() ->{
            int multipleFile = Integer.parseInt(multiple_file.getText().toString());
            SendReceive.sendData(fileToSend, fileSizeBytes, fileName, multipleFile);
        }).start();
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