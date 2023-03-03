package com.example.masterthesis.bluetooh;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
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
import com.example.masterthesis.MainActivity;
import com.example.masterthesis.MainActivity_Log;
import com.example.masterthesis.R;
import com.example.masterthesis.SpinnerCLass;

import java.io.IOException;
import java.util.ArrayList;


/**
 * View of the application after successful connection via Bluetooth
 */
public class MainActivity_BT extends AppCompatActivity {

    //Variable containing the list of devices found
    private final ArrayList<String> discoveredDevices = new ArrayList<>();

    //Adapter connecting arrays with ListView
    private ArrayAdapter<String> listAdapter;

    //sockets connecting as server and client
    public BluetoothSocket socketClient = null, socketServer = null;

    //Log class reference
    public final MainActivity_Log.ListLog LOG = new MainActivity_Log.ListLog();
    private ConnectBtServerThread threadServer;
    public ConnectBtClientThread threadClient;
    private EditText multiple_file;

    private Button button_sendData;
    private TextView textView_inf, textView_deviceRssi;
    private Intent fileToSend;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_bt);
        setTitle("Bluetooth");

        Button  button_disconnectBack = findViewById(R.id.button_disconnectBack),
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
        textView_deviceRssi = findViewById(R.id.textView_deviceRssi);
        multiple_file = findViewById(R.id.multiple_file);

        startSpinner();

        textView_connected.setText("Not connected");

        //The invoked thread listening for the connection attempt
        threadServer = new ConnectBtServerThread(this,socketServer,LOG);
        threadServer.start();

        //Button to detection by other devices
        button_detect.setOnClickListener(v -> discoverableBt());

        //Button to find device
        button_foundDevice.setOnClickListener(v -> foundDeviceBt());

        //Button to choose a file
        button_chooseFile.setOnClickListener(v -> chooseFile());

        multiple_file.setOnClickListener(v -> {
            try {
                int number = Integer.parseInt(multiple_file.getText().toString());
                if(number < 1 || number > 100)
                {
                    Toast.makeText(this, "Enter a value between 1-100", Toast.LENGTH_SHORT).show();
                    if(number > 100)
                        multiple_file.setText(Integer.toString(100));
                }
            } catch(NumberFormatException e) {
                Toast.makeText(this, "Enter a numeric value", Toast.LENGTH_SHORT).show();
                LOG.addLog(LOG.currentDate(),"Incorrect format loaded", e.getMessage());
            }
        });

        //ImageButton to increase the number of times a file is sent
        button_upMultipleFile.setOnClickListener(v -> {
            int number = Integer.parseInt(multiple_file.getText().toString());
            if(number < 100) {
                number += 1;
                multiple_file.setText(Integer.toString(number));
            }
        });

        //ImageButton to reduce the number of times a file is sent
        button_downMultipleFile.setOnClickListener(v -> {
            int number = Integer.parseInt(multiple_file.getText().toString());
            if(number > 1) {
                number -= 1;
                multiple_file.setText(Integer.toString(number));
            }
        });

        //Button to send data
        button_sendData.setOnClickListener(v ->
                ConnectBtClientThread.dataSendFromClient(true, fileToSend,
                    Integer.parseInt(multiple_file.getText().toString())));

        //Button to save Measurement Data
        button_saveMeasurementData.setOnClickListener(v -> saveMeasurementData());

        //Button to view a graph of the data
        button_graph.setOnClickListener(v -> drawGraph());

        //Button to disconnect or back
        button_disconnectBack.setOnClickListener(v -> {
            closeBtConnection();
            Intent intent = new Intent(MainActivity_BT.this, MainActivity.class);
            startActivity(intent);
        });
    }

    //region BT detection

    //Calling intent enable discoverability
    private void discoverableBt() {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        ActivityDiscoverableBt.launch(intent);
    }

    //Reactions to permission response received discoverableBt
    final ActivityResultLauncher<Intent> ActivityDiscoverableBt = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() != 0)
                    Toast.makeText(MainActivity_BT.this, "The device is discoverable", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(MainActivity_BT.this, "The device is undetectable", Toast.LENGTH_SHORT).show();
            });

    //endregion BT detection

    //region Searching for BT devices

    //configure discovery of Bluetooth devices
    @SuppressLint("MissingPermission") //Used to bypass validation re-verification
    private void foundDeviceBt() {
        Constants.bluetoothAdapter.startDiscovery();
        listDiscoverableDevices();
        intentActionFound();
        intentActionAclDisconnected();
        availableDevicesWindow();
    }

    //configuration of the list of discoverable devices
    private void listDiscoverableDevices()
    {
        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, discoveredDevices);
        listAdapter.clear();
    }

    //launching the intention to detect a new Bluetooth device
    private void intentActionFound()
    {
        IntentFilter intent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, intent);
    }

    //launching the intention about losing connection with the Bluetooth device
    private void intentActionAclDisconnected()
    {
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(receiver, filter);
    }

    // Create a BroadcastReceiver for ACTION_FOUND or ACTION_ACL_DISCONNECTED.
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
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

    //Select a found device for Bluetooth connection
    @SuppressLint({"SetTextI18n", "MissingPermission"})
    private void availableDevicesWindow()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        @SuppressLint("InflateParams")
        View titleView = getLayoutInflater().inflate(R.layout.dialog_title, null);
        builder.setCustomTitle(titleView);
        builder.setTitle("Select a device");


        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.setAdapter(listAdapter, (dialog, which) -> {
            String deviceInfo = listAdapter.getItem(which);
            //deviceAddress holds the 17 characters from the end of the deviceInfo string
            String deviceAddress = deviceInfo.substring(deviceInfo.length() - 17);
            BluetoothDevice device = Constants.bluetoothAdapter.getRemoteDevice(deviceAddress);
            threadClient = new ConnectBtClientThread(this, device, socketClient, LOG);
            threadClient.start();
            threadServer.interrupt();

            device.connectGatt(this, false, mGattCallback);
            TextView textView_deviceDistanceText = findViewById(R.id.textView_deviceRssiText);
            textView_deviceDistanceText.setVisibility(View.VISIBLE);
        });

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        // Getting ProgressBar, ImageView from View
        ProgressBar progressBar = titleView.findViewById(R.id.progressBar_search);
        ImageView imageView = titleView.findViewById(R.id.imageView_done);
        // Delaying an element's visibility change using the postDelayed() method
        progressBar.postDelayed(() -> {
            progressBar.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
        }, Constants.timeSearch);
    }

    // definicja BluetoothGattCallback dla odbierania powiadomień o zmianach wartości RSSI
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            // obsługa zmiany stanu połączenia
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // połączenie zostało nawiązane, odczytaj wartość RSSI
                new Thread(() -> {
                    Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
                    while(true) {
                        gatt.readRemoteRssi();
                        try {
                            Thread.sleep(500); // czekaj 5 milisekundę
                        } catch (InterruptedException e) {
                            LOG.addLog(LOG.currentDate(),"RSSI thread hold error",e.getMessage());
                        }
                        if(!ConnectBtClientThread.getSocketClient().isConnected())
                            break;
                    }
                }).start();
            }
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            // obsługa odczytu wartości RSSI
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // odczytano wartość RSSI, zapisz ją i wykonaj odpowiednie akcje
                runOnUiThread(() ->textView_deviceRssi.setText(Integer.toString(rssi)));
            } else {
                // wystąpił błąd podczas odczytu wartości RSSI
                LOG.addLog(LOG.currentDate(),"wystąpił błąd podczas odczytu wartości RSSI");
            }
        }
    };

    //endregion Searching for BT devices

    //region Recording of measurement data

    private void saveMeasurementData(){
        ConnectBtClientThread.saveMeasurementData();
    }

    //endregion Recording of measurement data


    private void drawGraph(){
        Intent intent = new Intent(this, Graph.class);
        startActivity(intent);
    }

    //The method where the intent to select the file to be sent is triggered
    private void chooseFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent,Constants.REQUEST_BT_SEND_DATA_FILE);
    }

    //Reactions to permission response received openFile
    @SuppressLint({"MissingPermission", "SetTextI18n"})
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_BT_SEND_DATA_FILE && resultCode == RESULT_OK) {
            LOG.addLog(LOG.currentDate(),"You have selected a file to upload");
            button_sendData.setVisibility(View.VISIBLE);
            fileToSend = data;
            Double fileSize = threadClient.getFileSize(fileToSend.getData());
            String  fileName = threadClient.getFileName(fileToSend.getData()),
                    fileSizeUnit = threadClient.getFileSizeUnit();
            textView_inf.setText("The name of the uploaded file: " + fileName +
                    "\nFile size: " +
                    Constants.decimalFormat.format(fileSize).replace(",", ".") +
                    " " + fileSizeUnit + "\n");
        }
    }

    //method where the relevant threads, streams and other elements working in the background are closed
    @SuppressLint("MissingPermission")
    private void closeBtConnection()
    {
        if(threadClient != null)
            if(threadClient.isAlive()) {
                threadClient.interrupt();
                LOG.addLog(LOG.currentDate(),"Thread client was stopped");
            }
        if(threadServer != null)
            if(threadServer.isAlive()) {
                threadServer.interrupt();
                LOG.addLog(LOG.currentDate(),"Thread server was stopped");
            }
        if(receiver.isOrderedBroadcast()) {
            unregisterReceiver(receiver);
            LOG.addLog(LOG.currentDate(),"Broadcast was closed");
        }
        if(Constants.bluetoothAdapter.isDiscovering()) {
            Constants.bluetoothAdapter.cancelDiscovery();
            LOG.addLog(LOG.currentDate(),"bluetoothAdapter was closed");
        }
        if(ConnectBtClientThread.getSocketClient() != null)
            if(ConnectBtClientThread.getSocketClient().isConnected()) {
                try {
                    ConnectBtClientThread.getSocketClient().close();
                    LOG.addLog(LOG.currentDate(),"Socket client was closed");
                } catch (IOException e) {
                    LOG.addLog(LOG.currentDate(),"Error closing socket client", e.getMessage());
                }
            }
        if(socketServer != null)
            if(socketServer.isConnected()) {
                try {
                    socketServer.close();
                    LOG.addLog(LOG.currentDate(),"Socket server was closed");
                } catch (IOException e) {
                    LOG.addLog(LOG.currentDate(),"Error closing socket server", e.getMessage());
                }
            }
    }

    //The method that is run when the application is closed
    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeBtConnection();
    }

    //Running spinner to select buffer size and number of uploaded files
    private void startSpinner()
    {
        new SpinnerCLass(this, findViewById(R.id.buffer_size));
    }

    //Create a menu for your current activity
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem itemShowLog = menu.findItem(R.id.show_log);
        itemShowLog.setTitle("Show Log");
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