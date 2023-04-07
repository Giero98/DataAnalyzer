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
    ArrayList<String> discoveredDevices = new ArrayList<>();
    ArrayAdapter<String> listAdapter;
    final Logs LOG = new Logs();
    DeclarationOfUIVar declarationUI;
    String fileName;
    public static WifiP2pManager wifiDirectManager;
    static WifiP2pManager.Channel wifiDirectChannel;
    Intent fileToSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wi_fi);
        setTitle(Constants.titleWifiActivity);

        declarationUI = new DeclarationOfUIVar(this);
        startSelectBufferSize();
        settingVariableManagingConnect();
        buttonsResponse();
        startReceiverWithFilters();
    }

    void startSelectBufferSize() {
        new Buffer(this, findViewById(R.id.buffer_size));
    }

    void settingVariableManagingConnect() {
        wifiDirectManager = (WifiP2pManager) getSystemService(WIFI_P2P_SERVICE);
        wifiDirectChannel = wifiDirectManager.initialize(this,getMainLooper(),null);
    }

    void buttonsResponse() {
        declarationUI.button_disconnectBack.setOnClickListener(v -> finish());
        declarationUI.button_detect.setOnClickListener(v -> startDiscoversDevices());
        declarationUI.button_devices.setOnClickListener(v -> displayWiFiDirectDevices());
        declarationUI.button_chooseFile.setOnClickListener(v -> chooseFile());
        DeclarationOfUIVar.multiple_file.setOnClickListener(v -> NumberOfFileFromUI.readNumberOfFilesToSent(this));
        declarationUI.button_upMultipleFile.setOnClickListener(v -> NumberOfFileFromUI.increasingNumberOfFilesToSent());
        declarationUI.button_downMultipleFile.setOnClickListener(v -> NumberOfFileFromUI.reducingNumberOfFilesToSent());
        declarationUI.button_sendData.setOnClickListener(v -> sendData());
        declarationUI.button_saveMeasurementData.setOnClickListener(v -> saveMeasurementData());
        declarationUI.button_graph.setOnClickListener(v -> drawGraph());
    }

    void startReceiverWithFilters() {
        createListDiscoverableDevices();
        IntentFilter intent = new IntentFilter();
        intent.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intent.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        registerReceiver(receiver, intent);
    }

    void createListDiscoverableDevices() {
        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, discoveredDevices);
        listAdapter.clear();
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onReceive(Context context, Intent intent) {
            if(wifiDirectManager!=null) {
                String action = intent.getAction();
                if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                    wifiDirectManager.requestPeers(wifiDirectChannel, peerListListener);
                }
                else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                    NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                    if(networkInfo.isConnected()) {
                        wifiDirectManager.requestConnectionInfo(wifiDirectChannel, connectionInfoListener);
                    }
                    else {
                        declarationUI.textView_connected.setText(getString(R.string.not_connected));
                    }
                }
            }
        }
    };

    WifiP2pManager.PeerListListener peerListListener = peerList -> {
        if(peerList.getDeviceList() != null) {
            discoveredDevices.clear();
            for(WifiP2pDevice device : peerList.getDeviceList()) {
                discoveredDevices.add(device.deviceName + "\n" + device.deviceAddress);
                listAdapter.notifyDataSetChanged();
            }
        }
        else {
            Toast.makeText(getApplicationContext(), getString(R.string.no_devices_found), Toast.LENGTH_SHORT).show();
        }
    };

    WifiP2pManager.ConnectionInfoListener connectionInfoListener = wifiDirectInfo -> {
        if(wifiDirectInfo.groupFormed && wifiDirectInfo.isGroupOwner) {
            ServerWiFi server = new ServerWiFi(this);
            server.start();
        }
        else if(wifiDirectInfo.groupFormed) {
            discoverService(wifiDirectInfo);
        }
    };

    void discoverService(WifiP2pInfo wifiDirectInfo) {
        final HashMap<String, String> buddies = new HashMap<>();

        WifiP2pManager.DnsSdTxtRecordListener txtListener = (fullDomain, record, device) -> {

            String[] portNumberInfo = record.toString().split("=");
            String portNumber = portNumberInfo[1].replace("}","");
            buddies.put(device.deviceAddress, record.get(getString(R.string.port)));
            LOG.addLog(getString(R.string.port_number) + ":  " + portNumber);

            ClientWiFi client = new ClientWiFi(this, wifiDirectInfo, portNumber);
            client.start();
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
                public void onSuccess() {

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
                Toast.makeText(getApplicationContext(), getString(R.string.discovery_started), Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onFailure(int i) {
                Toast.makeText(getApplicationContext(), getString(R.string.discovery_failed), Toast.LENGTH_SHORT).show();
                LOG.addLog(getString(R.string.discovery_failed), String.valueOf(i));
            }
        });
    }

    void displayWiFiDirectDevices() {
        startDiscoversDevices();
        selectDeviceToConnection();
    }

    @SuppressLint("MissingPermission")
    void selectDeviceToConnection() {
        AlertDialog.Builder deviceSelection = new AlertDialog.Builder(this);
        @SuppressLint("InflateParams")
        View titleView = getLayoutInflater().inflate(R.layout.window_device_selection, null);
        deviceSelection.setCustomTitle(titleView);
        deviceSelection.setTitle(getString(R.string.select_device));

        deviceSelection.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss());

        deviceSelection.setAdapter(listAdapter, (dialog, which) -> {
            String[] deviceInfo = listAdapter.getItem(which).split("\n");
            String selectedDeviceAddress = deviceInfo[1].trim();
            initiateConnection(selectedDeviceAddress);
        });

        showDeviceSelection(deviceSelection);
    }

    void initiateConnection(String selectedDeviceAddress) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = selectedDeviceAddress;
        config.groupOwnerIntent = 0; //connect as a client

        wifiDirectManager.connect(wifiDirectChannel, config, new WifiP2pManager.ActionListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onSuccess() {
                LOG.addLog(getString(R.string.connect_initialization_success));
            }
            @Override
            public void onFailure(int reason) {
                Toast.makeText(getApplicationContext(), getString(R.string.connect_failed), Toast.LENGTH_SHORT).show();
                LOG.addLog(getString(R.string.connect_failed), String.valueOf(reason));
            }
        });
    }

    void showDeviceSelection(AlertDialog.Builder deviceSelection) {
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
            LOG.addLog(getString(R.string.file_selected));
            declarationUI.button_sendData.setVisibility(View.VISIBLE);
            declarationUI.parameterLayoutForFileUpload.setVisibility(View.VISIBLE);

            fileToSend = data;
            Double fileSize = FileInformation.getFileSize(fileToSend.getData(),this);
            fileName = FileInformation.getFileName(fileToSend.getData(), this);
            displayFileInformation(fileSize);
        }
    }

    @SuppressLint("SetTextI18n")
    void displayFileInformation(Double fileSize) {
        declarationUI.textView_inf.setText(getString(R.string.file_name) + ": " + fileName +
                "\n" + getString(R.string.file_size) + ": " + Constants.decimalFormat.format(fileSize).replace(",", ".") +
                " " + FileInformation.getFileSizeUnit(FileInformation.getFileSizeBytes()) + "\n");
    }

    //endregion

    void sendData() {
        new Thread(() -> {
                int multipleFile = NumberOfFileFromUI.getNumberFromUI();
                new SendingData(LOG, this,ClientWiFi.getSocket(),fileToSend,multipleFile);
        }).start();
    }

    void saveMeasurementData() {
        SendingData.saveMeasurementData(this, LOG);
    }

    void drawGraph() {
        Graph.connectionDetails = Constants.connectionWiFi;
        Intent intent = new Intent(this, Graph.class);
        startActivity(intent);
    }

    void disconnectTheConnection() {
        closeAllAboutWifiManager();
        endListening();
        closeClientWifiSocket();
        closeServerWifiSocket();
        closeServerWifiServerSocket();
    }

    void closeAllAboutWifiManager() {
        if(wifiDirectManager!=null) {
            wifiDirectManager.requestConnectionInfo(wifiDirectChannel, info -> {
                if (info.groupFormed) {
                    wifiDirectManager.removeGroup(wifiDirectChannel, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            LOG.addLog(getString(R.string.disconnect_success));
                        }

                        @Override
                        public void onFailure(int reasonCode) {
                            Toast.makeText(getApplicationContext(), getString(R.string.disconnect_failed), Toast.LENGTH_SHORT).show();
                            LOG.addLog(getString(R.string.disconnect_failed), String.valueOf(reasonCode));
                        }
                    });
                }
            });

            wifiDirectManager.stopPeerDiscovery(wifiDirectChannel, null);
            LOG.addLog(getString(R.string.wifi_direct_off));
        }
    }

    void endListening() {
        if(receiver.isOrderedBroadcast()) {
            unregisterReceiver(receiver);
            LOG.addLog(getString(R.string.broadcast_wifi_off));
        }
    }

    void closeClientWifiSocket() {
        if(ClientWiFi.getSocket() != null)
            if(ClientWiFi.getSocket().isConnected()) {
                try {
                    SendingData.closeStream(LOG, this);
                    ClientWiFi.getSocket().close();
                    LOG.addLog(getString(R.string.socket_client_close));
                } catch (IOException e) {
                    LOG.addLog(getString(R.string.socket_client_close_error), e.getMessage());
                }
            }
    }

    void closeServerWifiSocket() {
        if(ServerWiFi.getSocket() != null)
            if(ServerWiFi.getSocket().isConnected()) {
                try {
                    SavingData.closeStreams(LOG, this);
                    ServerWiFi.getSocket().close();
                    LOG.addLog(getString(R.string.socket_server_close));
                } catch (IOException e) {
                    LOG.addLog(getString(R.string.socket_server_close_error), e.getMessage());
                }
            }
    }

    void closeServerWifiServerSocket()
    {
        if(ServerWiFi.getServerSocket() != null) {
            try {
                ServerWiFi.getServerSocket().close();
                LOG.addLog(getString(R.string.server_socket_server_close));
            }
            catch (IOException e) {
                LOG.addLog(getString(R.string.server_socket_server_close_error), e.getMessage());
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
        MenuItem    showLog = menu.findItem(R.id.show_log),
                aboutAuthor = menu.findItem(R.id.about_author),
                changeLanguage = menu.findItem(R.id.change_language);
        showLog.setTitle(getString(R.string.title_log));
        aboutAuthor.setVisible(false);
        changeLanguage.setVisible(false);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.show_log) {
            Intent intent = new Intent(this, Logs.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}