package com.example.masterthesis.bluetooh;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.masterthesis.Constants;
import com.example.masterthesis.Graph;
import com.example.masterthesis.Logs;
import com.example.masterthesis.R;
import com.example.masterthesis.Buffer;

import java.io.IOException;
import java.util.ArrayList;

public class Bluetooth extends AppCompatActivity {
    final ArrayList<String> discoveredDevices = new ArrayList<>();
    ArrayAdapter<String> listAdapter;
    final Logs.ListLog LOG = new Logs.ListLog();
    ServerBt server;
    public ClientBt client;
    EditText multiple_file;
    Button button_sendData;
    TextView textView_inf, textView_qualitySignal;
    Intent fileToSend;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt);
        setTitle("Bluetooth");

        Button  button_disconnectBack = findViewById(R.id.button_disconnectAndBack),
                button_detect = findViewById(R.id.button_detect),
                button_foundDevice = findViewById(R.id.button_foundDevice),
                button_chooseFile = findViewById(R.id.button_chooseFile),
                button_saveMeasurementData = findViewById(R.id.button_saveMeasurementData),
                button_graph = findViewById(R.id.button_graph);
        button_sendData = findViewById(R.id.button_sendData);
        ImageButton button_upMultipleFile = findViewById(R.id.button_upMultipleFile),
                    button_downMultipleFile = findViewById(R.id.button_downMultipleFile);
        TextView textView_connected = findViewById(R.id.textView_connected);
        textView_inf = findViewById(R.id.textView_inf);
        textView_qualitySignal = findViewById(R.id.textView_qualitySignal);
        multiple_file = findViewById(R.id.multiple_file);

        startSelectBufferSize();
        textView_connected.setText("Not connected");
        startServer();

        button_detect.setOnClickListener(v -> startDiscoverableBt());
        button_foundDevice.setOnClickListener(v -> startFoundDeviceBt());
        button_chooseFile.setOnClickListener(v -> chooseFile());
        multiple_file.setOnClickListener(v -> readNumberOfFilesToSent());
        button_upMultipleFile.setOnClickListener(v -> increasingNumberOfFilesToSent());
        button_downMultipleFile.setOnClickListener(v -> reducingNumberOfFilesToSent());
        button_sendData.setOnClickListener(v -> startSendData());
        button_saveMeasurementData.setOnClickListener(v -> saveMeasurementData());
        button_graph.setOnClickListener(v -> drawGraph());
        button_disconnectBack.setOnClickListener(v -> finish());
    }

    void startSelectBufferSize()
    {
        new Buffer(this, findViewById(R.id.buffer_size));
    }

    void startServer()
    {
        server = new ServerBt(this,LOG);
        ServerBt.running = true;
        server.start();
    }

    void startDiscoverableBt() {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        ActivityDiscoverableBt.launch(intent);
    }

    final ActivityResultLauncher<Intent> ActivityDiscoverableBt = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() != 0)
                    Toast.makeText(this, "The device is discoverable", Toast.LENGTH_SHORT).show();
            });

    @SuppressLint("MissingPermission")
    void startFoundDeviceBt() {
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
        intent.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(receiver, intent);
    }

    final BroadcastReceiver receiver = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                discoveredDevices.add(device.getName() + "\n" + device.getAddress());
            }
            else if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)){
                discoveredDevices.remove(device.getName() + "\n" + device.getAddress());
            }
            listAdapter.notifyDataSetChanged();
        }
    };

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
            String deviceInfo = listAdapter.getItem(which);
            //deviceAddress holds the 17 characters from the end of the deviceInfo string
            String deviceAddress = deviceInfo.substring(deviceInfo.length() - 17);
            BluetoothDevice device = Constants.bluetoothAdapter.getRemoteDevice(deviceAddress);
            ServerBt.running = false;
            startClient(device);
            device.connectGatt(this, false, receivingChangesOfRssiValues);
            startVisibilityQualitySignal();
        });

        showDeviceSelection(deviceSelection);
        showDurationDeviceSearch(titleView);
    }

    void startClient(BluetoothDevice device)
    {
        client = new ClientBt(this, device, LOG);
        ClientBt.running = true;
        client.start();
    }

    final BluetoothGattCallback receivingChangesOfRssiValues = new BluetoothGattCallback() {
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
                            LOG.addLog("Quality signal reading error", e.getMessage());
                        }
                    } while (ClientBt.getSocketClient().isConnected());
                }).start();
            }
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                int percentQualitySignal;
                if(rssi >= 0)
                    percentQualitySignal = Constants.maximumQualitySignal;
                else
                    percentQualitySignal = Constants.maximumQualitySignal + rssi;
                runOnUiThread(() ->textView_qualitySignal.setText(Integer.toString(percentQualitySignal)));
            } else {
                LOG.addLog("There was an error reading the signal quality value");
            }
        }
    };

    void startVisibilityQualitySignal()
    {
        TextView qualitySignalText = findViewById(R.id.textView_qualitySignalText);
        qualitySignalText.setVisibility(View.VISIBLE);
    }

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
            fileToSend = data;
            Double fileSize = client.getFileSize(fileToSend.getData());
            String  fileName = client.getFileName(fileToSend.getData()),
                    fileSizeUnit = ClientBt.getFileSizeUnit();
            textView_inf.setText("The name of the uploaded file: " + fileName + "\nFile size: " +
                    Constants.decimalFormat.format(fileSize).replace(",", ".") +
                    " " + fileSizeUnit + "\n");
        }
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

    void startSendData()
    {
        int multipleFile = Integer.parseInt(multiple_file.getText().toString());
        ClientBt.dataSendFromClient(true, fileToSend, multipleFile);
    }

    void saveMeasurementData(){
        ClientBt.saveMeasurementData();
    }

    void drawGraph(){
        Graph.connectionDetails = Constants.connectionBt;
        Intent intent = new Intent(this, Graph.class);
        startActivity(intent);
    }

    @SuppressLint("MissingPermission")
    void closeBtConnection()
    {
        if(client != null)
            if(client.isAlive()) {
                ClientBt.running = false;
                LOG.addLog("The client has ended");
            }
        if(server != null)
            if(server.isAlive()) {
                ServerBt.running = false;
                LOG.addLog("The server has ended");
            }
        if(receiver.isOrderedBroadcast()) {
            unregisterReceiver(receiver);
            LOG.addLog("Broadcast was closed");
        }
        if(Constants.bluetoothAdapter.isDiscovering()) {
            Constants.bluetoothAdapter.cancelDiscovery();
            LOG.addLog("bluetoothAdapter was closed");
        }
        if(ClientBt.getSocketClient() != null)
            if(ClientBt.getSocketClient().isConnected()) {
                try {
                    ClientBt.getSocketClient().close();
                    LOG.addLog("Socket client was closed");
                } catch (IOException e) {
                    LOG.addLog("Error closing socket client", e.getMessage());
                }
            }
        if(ServerBt.getSocketServer() != null)
            if(ServerBt.getSocketServer().isConnected()) {
                try {
                    ServerBt.getSocketServer().close();
                    LOG.addLog("Socket server was closed");
                } catch (IOException e) {
                    LOG.addLog("Error closing socket server", e.getMessage());
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