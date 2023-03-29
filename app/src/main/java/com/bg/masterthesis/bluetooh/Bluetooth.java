package com.bg.masterthesis.bluetooh;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bg.masterthesis.Buffer;
import com.bg.masterthesis.Constants;
import com.bg.masterthesis.file.FileInformation;
import com.bg.masterthesis.file.SendingData;
import com.bg.masterthesis.ui.DeclarationOfUIVar;
import com.bg.masterthesis.Graph;
import com.bg.masterthesis.Logs;
import com.bg.masterthesis.ui.NumberOfFileFromUI;
import com.bg.masterthesis.R;

import java.io.IOException;
import java.util.ArrayList;

public class Bluetooth extends AppCompatActivity {
    final ArrayList<String> discoveredDevices = new ArrayList<>();
    ArrayAdapter<String> listAdapter;
    final Logs LOG = new Logs();
    ServerBt server;
    DeclarationOfUIVar declarationUI;
    Intent fileToSend;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt);
        setTitle(Constants.titleBtActivity);

        declarationUI = new DeclarationOfUIVar(this);
        startSelectBufferSize();
        startServerBt();
        buttonsResponse();


    }

    void startSelectBufferSize()
    {
        new Buffer(this, findViewById(R.id.buffer_size));
    }

    void startServerBt()
    {
        server = new ServerBt(this);
        ServerBt.running = true;
        server.start();
    }

    void buttonsResponse()
    {
        declarationUI.button_disconnectBack.setOnClickListener(v -> finish());
        declarationUI.button_detect.setOnClickListener(v -> startDiscoverableBt());
        declarationUI.button_devices.setOnClickListener(v -> startFoundDevicesBt());
        declarationUI.button_chooseFile.setOnClickListener(v -> chooseFile());
        DeclarationOfUIVar.multiple_file.setOnClickListener(v -> NumberOfFileFromUI.readNumberOfFilesToSent(this));
        declarationUI.button_upMultipleFile.setOnClickListener(v -> NumberOfFileFromUI.increasingNumberOfFilesToSent());
        declarationUI.button_downMultipleFile.setOnClickListener(v -> NumberOfFileFromUI.reducingNumberOfFilesToSent());
        declarationUI.button_sendData.setOnClickListener(v -> startSendData());
        declarationUI.button_saveMeasurementData.setOnClickListener(v -> saveMeasurementData());
        declarationUI.button_graph.setOnClickListener(v -> drawGraph());
    }

    //region button_detect

    void startDiscoverableBt() {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        ActivityDiscoverableBt.launch(intent);
    }

    ActivityResultLauncher<Intent> ActivityDiscoverableBt = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() != 0)
                    Toast.makeText(this, "The device is discoverable", Toast.LENGTH_SHORT).show();
            });

    //endregion

    //region button_devices

    @SuppressLint("MissingPermission")
    void startFoundDevicesBt() {
        Constants.bluetoothAdapter.startDiscovery();
        createListDiscoverableDevices();
        startReceiverWithFilters();
        selectDeviceToConnection();
    }

    void createListDiscoverableDevices()
    {
        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, discoveredDevices);
        listAdapter.clear();
    }

    void startReceiverWithFilters()
    {
        IntentFilter intent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, intent);
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                discoveredDevices.add(device.getName() + "\n" + device.getAddress());
                listAdapter.notifyDataSetChanged();
            }
        }
    };

    void selectDeviceToConnection()
    {
        AlertDialog.Builder deviceSelection = new AlertDialog.Builder(this);
        @SuppressLint("InflateParams")
        View titleView = getLayoutInflater().inflate(R.layout.window_device_selection, null);
        deviceSelection.setCustomTitle(titleView);
        deviceSelection.setTitle(Constants.titleDialogToSelectDevice);

        deviceSelection.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        deviceSelection.setAdapter(listAdapter, (dialog, which) -> {
            String deviceInfo = listAdapter.getItem(which);
            startProcedureOfEstablishingBtConnection(deviceInfo);
        });

        showDeviceSelection(deviceSelection);
        showDurationDeviceSearch(titleView);
    }

    @SuppressLint("MissingPermission")
    void startProcedureOfEstablishingBtConnection(String deviceInfo)
    {
        //deviceAddress holds the 17 characters from the end of the deviceInfo string
        String deviceAddress = deviceInfo.substring(deviceInfo.length() - 17);
        BluetoothDevice device = Constants.bluetoothAdapter.getRemoteDevice(deviceAddress);
        closeServerBt();
        startClientBt(device);
        declarationUI.assignReferenceQualitySignal();
        device.connectGatt(this, false, receivingChangesOfRssiValues);
    }

    void startClientBt(BluetoothDevice device)
    {
        ClientBt client = new ClientBt(this, device);
        client.start();
    }

    BluetoothGattCallback receivingChangesOfRssiValues = new BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                new Thread(() -> {
                    Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
                    do {
                        gatt.readRemoteRssi();
                        try {
                            //noinspection BusyWait
                            Thread.sleep(Constants.delayReadingSignal);
                        } catch (InterruptedException e) {
                            LOG.addLog("RSSI read hold error", e.getMessage());
                            break;
                        }
                    } while (ClientBt.getSocket().isConnected());
                }).start();
            }
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                int percentQualitySignal;
                if(rssi >= 0) {
                    percentQualitySignal = Constants.maximumQualitySignal;
                }
                else {
                    percentQualitySignal = Constants.maximumQualitySignal + rssi;
                }
                runOnUiThread(() ->
                        DeclarationOfUIVar.textView_qualitySignal.setText(Integer.toString(percentQualitySignal)));
            } else {
                LOG.addLog("There was an error reading the signal quality value");
            }
        }
    };

    void showDeviceSelection(AlertDialog.Builder deviceSelection)
    {
        AlertDialog dialog = deviceSelection.create();
        dialog.show();
    }

    void showDurationDeviceSearch(View titleView)
    {
        ProgressBar progressBar = titleView.findViewById(R.id.progressBar_search);
        ImageView imageView = titleView.findViewById(R.id.imageView_done);
        progressBar.postDelayed(() -> {
            progressBar.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
        }, Constants.timeSearch);
    }

    //endregion

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
            LOG.addLog("Selected a file to upload");

            fileToSend = data;
            Double fileSize = FileInformation.getFileSize(fileToSend.getData(),this);
            String fileName = FileInformation.getFileName(fileToSend.getData(), this);
            String fileSizeUnit = FileInformation.getFileSizeUnit(FileInformation.getFileSizeBytes());
            displayFileInformation(fileSize, fileName, fileSizeUnit);

            declarationUI.button_sendData.setVisibility(View.VISIBLE);
        }
    }

    @SuppressLint("SetTextI18n")
    void displayFileInformation(Double fileSize, String  fileName, String  fileSizeUnit)
    {
        declarationUI.textView_inf.setText("The name of the uploaded file: " + fileName +
                "\nFile size: " + Constants.decimalFormat.format(fileSize).replace(",", ".") +
                " " + fileSizeUnit + "\n");
    }

    //endregion

    void startSendData()
    {
        new Thread(() -> {
            int multipleFile = NumberOfFileFromUI.getNumberFromUI();
            new SendingData(LOG, this, ClientBt.getSocket(),fileToSend,multipleFile);
        }).start();
    }

    void saveMeasurementData() {
        SendingData.saveMeasurementData(this, LOG);
    }

    void drawGraph(){
        Graph.connectionDetails = Constants.connectionBt;
        Intent intent = new Intent(this, Graph.class);
        startActivity(intent);
    }

    void closeBtConnection()
    {
        closeServerBt();
        endListening();
        closeBtAdapter();
        closeClientBtSocket();
        closeServerBtSocket();
    }
    void closeServerBt()
    {
        if(server != null)
            if(server.isAlive()) {
                ServerBt.running = false;
            }
    }
    void endListening()
    {
        if(receiver.isOrderedBroadcast()) {
            unregisterReceiver(receiver);
            LOG.addLog("Broadcast on Bt was closed");
        }
    }
    @SuppressLint("MissingPermission")
    void closeBtAdapter()
    {
        if(Constants.bluetoothAdapter.isDiscovering()) {
            Constants.bluetoothAdapter.cancelDiscovery();
            LOG.addLog("bluetoothAdapter was closed");
        }
    }

    void closeClientBtSocket()
    {
        if(ClientBt.getSocket() != null)
            if(ClientBt.getSocket().isConnected()) {
                try {
                    ClientBt.getSocket().close();
                    LOG.addLog("Socket client Bt was closed");
                } catch (IOException e) {
                    LOG.addLog("Error closing socket client Bt", e.getMessage());
                }
            }
    }
    void closeServerBtSocket()
    {
        if(ServerBt.getSocket() != null)
            if(ServerBt.getSocket().isConnected()) {
                try {
                    ServerBt.getSocket().close();
                    LOG.addLog("Socket server Bt was closed");
                } catch (IOException e) {
                    LOG.addLog("Error closing socket server Bt", e.getMessage());
                }
            }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeBtConnection();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem itemShowLog = menu.findItem(R.id.show_log);
        itemShowLog.setTitle(Constants.titleLog);
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